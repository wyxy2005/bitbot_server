package bitbot.handler.channel.tasks;

import bitbot.cache.swaps.SwapsItemData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.Constants;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
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
public class SwapTask implements Runnable {

    private final Response response;
    private final Request request;
    private final int hours;
    private final int interval;
    private final String Exchange;
    private final String currencies, serverAuthorization;
    private final long nonce;
    private final long ServerTimeFrom;
    private final int APIVersion;

    private boolean isAuthorized = true;

    public SwapTask(Request request, Response response, Query query) {
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
        if (query.containsKey("Exchange")) {
            Exchange = query.get("Exchange");
        } else {
            Exchange = "btce";
        }

        // depth
        int interval_ = Integer.parseInt(query.get("depth"));
        if (interval_ > 20160 || interval_ < 1) {
            isAuthorized = false;
        }
        this.interval = interval_;

        // currency pair
        this.currencies = query.get("currencies");

        // start from..
        this.ServerTimeFrom = Long.parseLong(query.get("ServerTimeFrom"));

        // API Version
        if (query.containsKey("APIVersion")) {
            APIVersion = Integer.parseInt(query.get("APIVersion"));
        } else {
            APIVersion = 1;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencies + (hours ^ interval ^ nonce ^ ServerTimeFrom) + Exchange);
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
                    JSONObject obj = new JSONObject();

                    List<List<SwapsItemData>> ret = ChannelServer.getInstance().getSwapsTask().getSwapsData(Exchange, currencies, ServerTimeFrom, hours, interval);

                    switch (APIVersion) {
                        case 1: {
                            int arrayPrintCount = 1;

                            for (List<SwapsItemData> swap : ret) {
                                JSONArray array1 = new JSONArray();

                                swap.stream().map((item) -> {
                                    JSONArray obj_array = new JSONArray();

                                    obj_array.add(item.getTimestamp());
                                    obj_array.add(item.getSpotPrice());
                                    obj_array.add(item.getRate());
                                    obj_array.add(item.getAmountLent());

                                    return obj_array;
                                }).forEach((json_array_Obj) -> {
                                    array1.add(json_array_Obj);
                                });

                                // print to jsonObject
                                obj.put("cur" + arrayPrintCount, array1);

                                arrayPrintCount++;
                            }

                            // Output
                            body.println(obj.toJSONString());
                            break;
                        }
                        case 2: { // encrypted return data
                            int arrayPrintCount = 1;

                            for (List<SwapsItemData> swap : ret) {
                                JSONArray array1 = new JSONArray();

                                swap.stream().map((item) -> {
                                    JSONArray obj_array = new JSONArray();

                                    obj_array.add(item.getTimestamp());
                                    obj_array.add(item.getSpotPrice());
                                    obj_array.add(item.getRate());
                                    obj_array.add(item.getAmountLent());

                                    return obj_array;
                                }).forEach((json_array_Obj) -> {
                                    array1.add(json_array_Obj);
                                });

                                // print to jsonObject
                                obj.put("cur" + arrayPrintCount, array1);

                                arrayPrintCount++;
                            }

                            // Output
                            body.print(
                                    CustomXorEncryption.custom_xor_encrypt(obj.toJSONString(), nonce));
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
