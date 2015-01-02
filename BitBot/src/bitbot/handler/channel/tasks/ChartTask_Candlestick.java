package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.Constants;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
import java.io.PrintStream;
import java.util.List;
import org.json.simple.JSONArray;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author z
 */
public class ChartTask_Candlestick implements Runnable {

    private final Response response;
    private final Request request;
    private final int hours;
    private final int interval;
    private final String ExchangeSite;
    private final String currencypair, serverAuthorization;
    private final long nonce;
    private final long ServerTimeFrom;
    private final int APIVersion;

    private boolean isAuthorized = true;

    public ChartTask_Candlestick(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;
        this.nonce = Long.parseLong(query.get("nonce"));

        final long cTime = System.currentTimeMillis();
        if (cTime - (60 * 60 * 24 * 1000) > nonce || cTime + (60 * 60 * 24 * 1000) < nonce) {
            isAuthorized = false;
        }

        // hours
        int hours_ = Integer.parseInt(query.get("hours"));
        if (hours_ <= 0 || hours_ >= (24 * 30 * 12 * 10)) {
            isAuthorized = false;
        }
        this.hours = hours_;

        // exchange site
        if (query.containsKey("ExchangeSite")) {
            ExchangeSite = query.get("ExchangeSite");
        } else {
            ExchangeSite = "btce";
        }

        // depth
        int depth_ = Integer.parseInt(query.get("depth"));
        if (depth_ > 20160 || depth_ < 1) {
            isAuthorized = false;
        }
        this.interval = depth_;

        // currency pair
        this.currencypair = query.get("currencypair");

        // start from..
        this.ServerTimeFrom = Long.parseLong(query.get("ServerTimeFrom"));

        // API Version
        if (query.containsKey("APIVersion")) {
            APIVersion = Integer.parseInt(query.get("APIVersion"));

            if (APIVersion <= 2) { // Depreciated
                isAuthorized = false;
            }
        } else {
            APIVersion = 1;
            isAuthorized = false;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencypair + (hours ^ interval ^ nonce ^ ServerTimeFrom) + ExchangeSite);
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
                    List<TickerItem_CandleBar> ret = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(currencypair, hours, interval, ExchangeSite, ServerTimeFrom);

                    switch (APIVersion) {
                        /*case 1: {
                         JSONArray array = new JSONArray();
                            
                         ret.stream().map((item) -> {
                         JSONObject obj = new JSONObject();
                         obj.put("server_time", item.getServerTime());
                         obj.put("Open", item.getOpen());
                         obj.put("Close", item.getClose());
                         obj.put("High", item.getHigh());
                         obj.put("Low", item.getLow());
                         obj.put("Volume", item.getVol());
                         obj.put("VolumeCur", item.getVol_Cur());
                         obj.put("Ratio", item.getBuySell_Ratio());
                         return obj;
                         }).forEach((obj) -> {
                         array.add(obj);
                         });
                         body.println(array.toJSONString());
                         break;
                         }
                         case 2: { // better optimized for data
                         JSONArray array = new JSONArray();
                            
                         ret.stream().map((item) -> {
                         JSONArray obj_array = new JSONArray();

                         obj_array.add(item.getServerTime());
                         obj_array.add(item.getOpen());
                         obj_array.add(item.getClose());
                         obj_array.add(item.getHigh());
                         obj_array.add(item.getLow());
                         obj_array.add(item.getVol());
                         obj_array.add(item.getVol_Cur());
                         obj_array.add(item.getBuySell_Ratio());

                         return obj_array;
                         }).forEach((obj) -> {
                         array.add(obj);
                         });
                         body.println(array.toJSONString());
                         break;
                         }*/
                        case 3: { // encrypted
                            JSONArray array = new JSONArray();

                            ret.stream().map((item) -> {
                                JSONArray obj_array = new JSONArray();

                                obj_array.add(item.getServerTime());
                                obj_array.add(item.getOpen());
                                obj_array.add(item.getClose());
                                obj_array.add(item.getHigh());
                                obj_array.add(item.getLow());
                                obj_array.add(item.getVol());
                                obj_array.add(item.getVol_Cur());
                                obj_array.add(item.getBuySell_Ratio());

                                return obj_array;
                            }).forEach((obj) -> {
                                array.add(obj);
                            });
                            body.print(
                                    CustomXorEncryption.custom_xor_encrypt(array.toJSONString(), nonce)
                            );
                            break;
                        }
                    }
                } else {
                    response.setStatus(Status.UNAUTHORIZED);
                }
                body.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
