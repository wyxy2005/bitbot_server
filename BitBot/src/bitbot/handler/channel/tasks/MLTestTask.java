package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import bitbot.Constants;
import java.io.PrintStream;
import java.util.List;
import org.json.simple.JSONArray;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 * Test task for machine learning -- for eee @ TradingView
 *
 * @author z
 */
public class MLTestTask implements Runnable {

    private final Response response;
    private final Request request;

    private final int backtestHours;
    private final int intervalMinutes;
    private final String ExchangeSite;
    private final String currencypair, returnType;
    private final long ServerTimeFrom;
    private final int APIVersion;

    private boolean isAuthorized = true;

    private static long LastRequestTime = 0;

    public MLTestTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;
        // hours
        int backtestHours_ = Integer.parseInt(query.get("backtestHours"));
        if (backtestHours_ <= 0 || backtestHours_ >= (24 * 30 * 12 * 10)) {
            isAuthorized = false;
        }
        this.backtestHours = backtestHours_;

        // exchange site
        if (query.containsKey("ExchangeSite")) {
            ExchangeSite = query.get("ExchangeSite");
        } else {
            ExchangeSite = "btce";
        }

        // depth
        int intervalMinutes_ = Integer.parseInt(query.get("intervalMinutes"));
        if (intervalMinutes_ > 20160 || intervalMinutes_ < 1) {
            isAuthorized = false;
        }
        this.intervalMinutes = intervalMinutes_;

        // currency pair
        this.currencypair = query.get("currencypair");

        // start from..
        this.ServerTimeFrom = Long.parseLong(query.get("ServerTimeFrom"));

        // API Version
        if (query.containsKey("APIVersion")) {
            APIVersion = Integer.parseInt(query.get("APIVersion"));
        } else {
            APIVersion = 1;
        }

        // Return Type
        if (query.containsKey("returnType")) { // json, csv
            returnType = query.get("returnType");
        } else {
            returnType = "json";
        }

        final long cTime = System.currentTimeMillis();
        if (cTime - LastRequestTime < 1000) {
            isAuthorized = false;
        }
        LastRequestTime = cTime;
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                _ResponseHeader.addBasicResponseHeader(response);

                if (isAuthorized) {
                    List<TickerItem_CandleBar> ret = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(
                            currencypair, 
                            backtestHours, 
                            intervalMinutes, 
                            ExchangeSite, 
                            ServerTimeFrom,  
                            System.currentTimeMillis() / 1000, 
                            true, false);

                    switch (APIVersion) {
                        case 1: { // encrypted
                            if (returnType.contains("json")) {
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

                                String retString = array.toJSONString();

                                response.setContentLength(retString.length());
                                body.print(retString);
                            } else { // csv
                                final String newLine = "\r\n";

                                StringBuilder sb = new StringBuilder("server_time,high,low,open,close,volume,volumecur,buysell_ratio");
                                sb.append(newLine);

                                for (TickerItem_CandleBar obj : ret) {
                                    sb.append(obj.getServerTime()).append(','); // dont want truncated number

                                    sb.append(obj.getHigh()).append(',');
                                    sb.append(obj.getLow()).append(',');
                                    sb.append(obj.getOpen()).append(',');
                                    sb.append(obj.getClose()).append(',');
                                    sb.append(obj.getVol()).append(',');
                                    sb.append(obj.getVol_Cur()).append(',');
                                    sb.append(obj.getBuySell_Ratio());

                                    sb.append(newLine);
                                }
                                sb.append(newLine);

                                String retString = sb.toString();

                                response.setContentLength(retString.length());
                                body.print(retString);
                            }
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
