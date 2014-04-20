 package bitbot.handler.channel.tasks;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.Constants;
import bitbot.cache.tickers.TickerItemData;
import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.util.encryption.HMACSHA1;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author z
 */
public class ChartTask implements Runnable {

    private final Response response;
    private final Request request;
    private final int hours;
    private final int depth;
    private final String ExchangeSite;
    private final String currencypair, serverAuthorization;
    private final long nonce;
    private final long ServerTimeFrom;
    private final boolean IsIntervalBased;

    private boolean isAuthorized = true;

    public ChartTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;
        this.nonce = Long.parseLong(query.get("nonce"));

        final long cTime = System.currentTimeMillis();
        if (cTime - (60 * 60 * 24 * 1000) > nonce || cTime + (60 * 60 * 24 * 1000) < nonce) {
            isAuthorized = false;
        }

        // hours
        int hours_ = Integer.parseInt(query.get("hours"));
        if (hours_ <= 0 || hours_ >= (24 * 30 * 12 * 5)) {
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
        if (depth_ > 10080 || depth_ < 1) {
            isAuthorized = false;
        }
        this.depth = depth_;

        // currency pair
        this.currencypair = query.get("currencypair");

        // start from..
        this.ServerTimeFrom = Long.parseLong(query.get("ServerTimeFrom"));

        // Is interval
        if (query.containsKey("IsIntervalBased")) {
            IsIntervalBased = Boolean.parseBoolean(query.get("IsIntervalBased"));
        } else {
            IsIntervalBased = false;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencypair + (hours ^ depth ^ nonce ^ ServerTimeFrom) + ExchangeSite);
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
                long time = System.currentTimeMillis();

                response.setValue("Content-Type", "text/plain");
                response.setValue("Server", Constants.Server_UserAgent);
                response.setDate("Date", time);
                response.setDate("Last-Modified", time);

                if (isAuthorized) {
                    JSONArray array = new JSONArray();
                    
                    if (IsIntervalBased) {
                        List<TickerItem_CandleBar> ret = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(currencypair, hours, depth, ExchangeSite, ServerTimeFrom);

                        for (TickerItem_CandleBar item : ret) {
                            JSONObject obj = new JSONObject();
                            obj.put("server_time", item.getServerTime());
                            obj.put("Open", item.getOpen());
                            obj.put("Close", item.getClose());
                            obj.put("High", item.getHigh());
                            obj.put("Low", item.getLow());
                            obj.put("Volume", item.getVol());
                            obj.put("VolumeCur", item.getVol_Cur());
                            
                            array.add(obj);
                        }
                        body.println(array.toJSONString());
                    } else {
                        List<TickerItemData> ret = ChannelServer.getInstance().getTickerTask().getTickerList(currencypair, hours, depth, ExchangeSite, ServerTimeFrom);

                        for (TickerItemData item : ret) {
                            JSONObject obj = new JSONObject();
                            obj.put("server_time", item.getServerTime());
                            obj.put("updated", 0);
                            obj.put("high", item.getHigh());
                            obj.put("low", item.getLow());
                            obj.put("avg", 0);
                            obj.put("buy", item.getOpen());
                            obj.put("sell", 0);
                            obj.put("last", 0);
                            obj.put("vol", item.getVol());
                            obj.put("vol_cur", item.getVol_Cur());

                            array.add(obj);
                        }
                        body.println(array.toJSONString());
                    }
                } else {
                    response.setStatus(Status.UNAUTHORIZED);
                }
                body.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
