package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.Constants;
import bitbot.util.HttpClient;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class TickerHistory_CampBX implements TickerHistory {
    // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");

    private long lastBroadcastedTime = 0;

    private boolean readyToBroadcastPriceChanges() {
        final long cTime = System.currentTimeMillis();
        if (cTime - lastBroadcastedTime > Constants.PriceBetweenServerBroadcastDelay) {
            lastBroadcastedTime = cTime;
            return true;
        }
        return false;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        String Uri = "http://CampBX.com/api/xticker.php";
        String Result = HttpClient.httpGet(Uri, "");

        if (Result != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(LastPurchaseTime, LastTradeId, 0, true);

            JSONParser parser = new JSONParser(); // Init parser
            try {
                // Timestamp for last purchase time
                //Calendar cal_LastPurchaseTime = Calendar.getInstance();

                // Container factory for the JSON array to persist the order
                ContainerFactory containerFactoryBuy = new ContainerFactory() {
                    @Override
                    public List creatArrayContainer() {
                        return new LinkedList();
                    }

                    @Override
                    public Map createObjectContainer() {
                        return new LinkedHashMap();
                    }
                };

                LinkedHashMap Obj = (LinkedHashMap) parser.parse(Result, containerFactoryBuy);

                float lasttrade = Float.parseFloat(Obj.get("Last Trade").toString());
                float buy = Float.parseFloat(Obj.get("Best Bid").toString());
                float sell = Float.parseFloat(Obj.get("Best Ask").toString());
                final TradeHistoryBuySellEnum type = TradeHistoryBuySellEnum.Unknown; // Campbx doesn't broadcast buy or sell
                
                final long cTime = System.currentTimeMillis();

                //http://tutorials.jenkov.com/java-date-time/java-util-timezone.html
                // Timestamp for trades
                Calendar cal = Calendar.getInstance(); // BTCe time

                //System.out.println(String.format("[Trades history] Got [%s], Buy: %f, Sell: %f", cal.getTime().toString(), buy, sell));
                ReturnData.merge_CoinbaseOrCampBX(buy, sell, cTime, type);
                
                if (readyToBroadcastPriceChanges()) {
                    ChannelServer.getInstance().broadcastPriceChanges(
                            type,
                            CurrencyPair,
                            lasttrade,
                            0,
                            cTime,
                            0);
                }

            } catch (Exception parseExp) {
                parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
            return ReturnData;
        }
        return null;
    }
}
