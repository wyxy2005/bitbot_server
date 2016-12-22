package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import bitbot.util.encryption.HMACSHA1;
import bitbot.util.encryption.output.PacketLittleEndianWriter;
import java.io.PrintStream;
import java.util.List;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 * The 24 hour summary snapshot of active exchanges.
 *
 * @author twili
 */
public class PriceSummaryChartTask implements Runnable {

    private final Response response;
    private final Request request;
    private final String serverAuthorization;
    private final long nonce;
    private final int APIVersion;

    private boolean isAuthorized = true;

    // Cache
    private byte[] retResultCache = null;
    private long lastResultCacheTime = 0;

    public PriceSummaryChartTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;
        this.nonce = Long.parseLong(query.get("nonce"));

        final long cTime = System.currentTimeMillis();
        if (cTime - (60 * 60 * 24 * 1000) > nonce || cTime + (60 * 60 * 24 * 1000) < nonce) {
            isAuthorized = false;
        }

        // API Version
        if (query.containsKey("APIVersion")) {
            APIVersion = Integer.parseInt(query.get("APIVersion"));

            if (APIVersion != 1 && APIVersion != 2) {
                isAuthorized = false;
            }
        } else {
            APIVersion = 99;
            isAuthorized = false;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), String.valueOf(nonce >>> 10 + APIVersion & 0xFF));
        //System.out.println("FromClient: " + serverAuthorization);
        //System.out.println("Real: " + encoded);
        if (!serverAuthorization.equals(encoded)) {
            isAuthorized = false;
        }
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                _ResponseHeader.addBasicResponseHeader(response);

                if (isAuthorized) {
                    final long cTime = System.currentTimeMillis();

                    // Create a new copy if there are no available cache
                    // or if an hour have passed
                    if (retResultCache == null
                            || cTime - lastResultCacheTime > (1000 * 50)) { // 50 secs update

                        final List<String> allowedCurrencies = ChannelServer.getInstance().getTickerTask().getlistCurrenciesAllowedFor24HSnapshot();
                        final long cTime_Seconds = System.currentTimeMillis() / 1000;

                        final PacketLittleEndianWriter plew = new PacketLittleEndianWriter();

                        for (String ExchangeCurrencyPair : allowedCurrencies) {
                            final String[] Source_pair = ExchangeCurrencyPair.split("-");
                            final String ExchangeSite = Source_pair[0];
                            final String CurrencyPair = Source_pair[1];

                            final List<TickerItem_CandleBar> bars
                                    = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(CurrencyPair, 0,
                                            60 * 4, // period in minutes
                                            ExchangeSite,
                                            cTime_Seconds - (60l * 60l * 24l), // from
                                            cTime_Seconds, // time to [now]
                                            false, true);

                            // Create the JSON Object
                            if (bars != null) {
                                plew.write(1); // define that there's something still..

                                plew.writeMapleAsciiString(ExchangeSite);
                                plew.writeMapleAsciiString(CurrencyPair);

                                // 4 hour candle bars
                                plew.writeInt(bars.size());
                                double totalVolume = 0, totalVolumeCur = 0;
                                for (TickerItem_CandleBar bar : bars) {
                                    plew.writeDouble(bar.getClose());
                                    
                                    totalVolume += bar.getVol();
                                    totalVolumeCur += bar.getVol_Cur();
                                }
                                // Other information
                                if (APIVersion >= 2) {
                                    if (bars.isEmpty()) {
                                        plew.write(0);
                                    } else {
                                        plew.write(1);

                                        // Write candle data of now, and 24 hours ago
                                        final TickerItem_CandleBar priceNow = bars.get(bars.size() - 1);
                                        writeTickerCandleInfo(plew, priceNow);

                                        final TickerItem_CandleBar price24H_Ago = bars.get(0);
                                        writeTickerCandleInfo(plew, price24H_Ago);
                                        
                                        // Write total volume
                                        plew.writeDouble(totalVolume);
                                        plew.writeDouble(totalVolumeCur);
                                        
                                        // Write relative changes
                                        final double relativePointsDiff = priceNow.getClose() / price24H_Ago.getClose();
                                        plew.writeDouble(relativePointsDiff);
                                        plew.writeFloat((float) (relativePointsDiff * 100f) - 100f);
                                    }
                                }
                            }
                        }
                        plew.write(0); // End marker

                        // Output
                        final byte[] packet = plew.getPacket();

                        // Set cache data
                        retResultCache = packet;
                        lastResultCacheTime = cTime;
                    }

                    response.setContentLength(retResultCache.length);
                    body.write(retResultCache);
                } else {
                    response.setStatus(Status.UNAUTHORIZED);
                }
                body.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeTickerCandleInfo(PacketLittleEndianWriter plew, TickerItem_CandleBar candle) {
        plew.writeFloat(candle.getClose());
        plew.writeFloat(candle.getOpen());
        plew.writeFloat(candle.getHigh());
        plew.writeFloat(candle.getLow());
        plew.writeLong(candle.getServerTime());
        plew.writeDouble(candle.getVol());
        plew.writeDouble(candle.getVol_Cur());
        plew.writeFloat(candle.getBuySell_Ratio());
    }
}
