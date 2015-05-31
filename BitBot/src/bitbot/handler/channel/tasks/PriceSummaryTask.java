package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItemData;
import bitbot.handler.channel.ChannelServer;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
import java.io.PrintStream;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author zheng
 */
public class PriceSummaryTask implements Runnable {

    private final Response response;
    private final Request request;
    private final String serverAuthorization;
    private final long nonce;
    private final int APIVersion;

    private boolean isAuthorized = true;

    public PriceSummaryTask(Request request, Response response, Query query) {
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

            if (APIVersion != 1) {
                isAuthorized = false;
            }
        } else {
            APIVersion = 99;
            isAuthorized = false;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), String.valueOf(nonce >>> 20 + APIVersion & 0xFF));
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
                    final Map<String, TickerItemData> ret
                            = ChannelServer.getInstance().getTickerTask().getExchangePriceSummaryData();

                    JSONArray array = new JSONArray();

                    if (ret != null) {
                        for (Entry<String, TickerItemData> data : ret.entrySet()) {
                            JSONObject obj = new JSONObject();
                            JSONArray array_data = new JSONArray();

                            TickerItemData item = data.getValue();

                            if (item != null) {
                                array_data.add(item.getServerTime());
                                array_data.add(item.getOpen());
                                array_data.add(item.getClose());
                                array_data.add(item.getHigh());
                                array_data.add(item.getLow());
                                array_data.add(item.getVol());
                                array_data.add(item.getVol_Cur());
                                array_data.add(item.getBuySell_Ratio());
                            }
                            obj.put("price", array_data);
                            obj.put("key", data.getKey());
                            obj.put("a", item == null ? 0 : 1);

                            array.add(obj);
                        }
                    }
                    String retString = CustomXorEncryption.custom_xor_encrypt(array.toJSONString(), nonce);
                    //String retString = array.toJSONString();

                    response.setContentLength(retString.length());
                    body.print(retString);
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
