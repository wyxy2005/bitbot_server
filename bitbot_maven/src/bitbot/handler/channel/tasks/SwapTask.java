package bitbot.handler.channel.tasks;

import bitbot.cache.swaps.SwapsItemData;
import bitbot.handler.channel.ChannelServer;
import bitbot.Constants;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
import bitbot.util.encryption.output.PacketLittleEndianWriter;
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
    //private final int hours;
    private final int numCandles;
    private final int timeframe;
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
        /*int hours_ = Integer.parseInt(query.get("hours"));
         if (hours_ <= 0 || hours_ >= (24 * 30 * 12 * 10)) {
         isAuthorized = false;
         }
         this.hours = hours_; */ // Obsolete
        // num candles
        numCandles = Integer.parseInt(query.get("numcandles"));
        if (numCandles <= 0 || numCandles > 4000) {
            isAuthorized = false;
        }

        // timeframe
        int timeframe_ = Integer.parseInt(query.get("timeframe"));
        if (timeframe_ > 20160 || timeframe_ < 1) {
            isAuthorized = false;
        }
        this.timeframe = timeframe_;

        // exchange site
        if (query.containsKey("Exchange")) {
            Exchange = query.get("Exchange");
        } else {
            Exchange = "btce";
        }

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
        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencies + (numCandles ^ timeframe ^ nonce ^ ServerTimeFrom ^ APIVersion) + Exchange);
        if (!serverAuthorization.equals(encoded)) {
            isAuthorized = false;
        }

      /*  System.out.println("FromClient: " + serverAuthorization);
         System.out.println("Real: " + encoded);
         System.out.println("isAuthorized: " + isAuthorized);*/
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                _ResponseHeader.addBasicResponseHeader(response);

                if (isAuthorized) {
                    final List<List<SwapsItemData>> ret = ChannelServer.getInstance().getSwapsTask().getSwapsData(Exchange, currencies, ServerTimeFrom, numCandles, timeframe);

                    switch (APIVersion) {
                        case 1: {
                            JSONObject obj = new JSONObject();
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
                            String retString = obj.toJSONString();

                            response.setContentLength(retString.length());
                            body.println(retString);
                            break;
                        }
                        case 2: { // encrypted return data
                            JSONObject obj = new JSONObject();
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
                            String retString = CustomXorEncryption.custom_xor_encrypt(obj.toJSONString(), nonce);

                            response.setContentLength(retString.length());
                            body.print(retString);
                            break;
                        }
                        case 3: { // byte, compressed data type
                            final PacketLittleEndianWriter plew = new PacketLittleEndianWriter();

                            int arrayPrintCount = 1;

                            // Write size
                            plew.writeInt(ret.size());
                            for (List<SwapsItemData> swap : ret) {
                                plew.writeMapleAsciiString("cur" + arrayPrintCount);
                                plew.writeInt(swap.size());

                                for (SwapsItemData item : swap) {
                                    plew.writeInt((int) item.getTimestamp());
                                    plew.writeFloat(item.getSpotPrice());
                                    plew.writeFloat(item.getRate());
                                    plew.writeDouble(item.getAmountLent());
                                }
                                arrayPrintCount++;
                            }

                            // Output
                            final byte[] packet = plew.getPacket();

                            response.setContentLength(packet.length);
                            body.write(packet);
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
