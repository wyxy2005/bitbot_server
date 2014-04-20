package bitbot.handler.channel.tasks;

import bitbot.graph.ExponentialMovingAverageData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.Constants;
import bitbot.util.encryption.HMACSHA1;
import java.io.PrintStream;
import java.util.ArrayList;
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
public class ExponentialMovingAverageTask implements Runnable {

    private final Response response;
    private final Request request;
    private final int highEMA, lowEMA;
    private final int backtestHours;
    private final int intervalMinutes;
    private final String ExchangeSite;
    private final String currencypair, serverAuthorization;
    private final long nonce;

    private boolean isAuthorized = true;

    public ExponentialMovingAverageTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;

        // nonce
        this.nonce = Long.parseLong(query.get("nonce"));

        // exchange site
        ExchangeSite = query.get("ExchangeSite");

        // currency pair
        currencypair = query.get("currencypair");

        // backtestHours
        backtestHours = Integer.parseInt(query.get("backtestHours"));

        // intervalMinutes
        intervalMinutes = Integer.parseInt(query.get("intervalMinutes"));

        // EMA
        highEMA = Integer.parseInt(query.get("highEMA"));
        lowEMA = Integer.parseInt(query.get("lowEMA"));

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencypair + (highEMA ^ intervalMinutes ^ lowEMA ^ backtestHours ^ nonce) + ExchangeSite);

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
                    List<List<ExponentialMovingAverageData>> ret
                            = ChannelServer.getInstance().getTickerTask().getExponentialMovingAverage(currencypair, ExchangeSite, backtestHours, intervalMinutes, highEMA, lowEMA);

                    JSONObject mainObj = new JSONObject();

                    JSONArray array_EMA = new JSONArray();
                    for (int i = ret.get(0).size(); i > 0; i--) {
                        ExponentialMovingAverageData highItem = ret.get(0).get(i - 1);
                        ExponentialMovingAverageData lowItem = ret.get(1).get(i - 1);
                        
                        array_EMA.add(createEMAJsonObject(highItem, lowItem));
                    }
                    mainObj.put("EMAs", array_EMA);

                    body.println(mainObj.toJSONString());
                } else {
                    response.setStatus(Status.UNAUTHORIZED);
                }
                body.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createEMAJsonObject(ExponentialMovingAverageData highItem,
            ExponentialMovingAverageData lowItem) {
        JSONObject obj = new JSONObject();

        obj.put("t", highItem.getServerTime());
        obj.put("hd", highItem.getEMA());
        obj.put("ld", lowItem.getEMA());
        obj.put("p", highItem.getPrice());

        return obj;
    }
}
