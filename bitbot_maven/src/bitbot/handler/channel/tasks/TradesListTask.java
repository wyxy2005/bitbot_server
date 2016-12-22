package bitbot.handler.channel.tasks;

import bitbot.cache.trades.TradesItemData;
import bitbot.handler.channel.ChannelServer;
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
 * @author zheng
 */
public class TradesListTask implements Runnable {

    private final Response response;
    private final Request request;
    private final String serverAuthorization;

    private final String ExchangeCurrency;
    private final float filterAbove;
    private final long timeAbove;

    private final long nonce;
    private final int APIVersion;

    private boolean isAuthorized = true;

    public TradesListTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;
        this.nonce = Long.parseLong(query.get("nonce"));

        final long cTime = System.currentTimeMillis();
        if (cTime - (60 * 60 * 24 * 1000) > nonce || cTime + (60 * 60 * 24 * 1000) < nonce) {
            isAuthorized = false;
        }

        // Exchangecurrency
        if (query.containsKey("ExchangeCurrency")) {
            ExchangeCurrency = query.get("ExchangeCurrency");
        } else {
            ExchangeCurrency = "";
            isAuthorized = false;
        }

        // filters
        if (query.containsKey("filterAbove") && query.containsKey("timeAbove")) {
            timeAbove = Long.parseLong(query.get("timeAbove"));
            filterAbove = Float.parseFloat(query.get("filterAbove"));
        } else {
            timeAbove = 0;
            filterAbove = 0;
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

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), String.valueOf((int) (nonce % 20 >> (int) filterAbove / timeAbove) + APIVersion));
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
                    final List<TradesItemData> ret
                            = ChannelServer.getInstance().getTradesTask().getTradesList(ExchangeCurrency, filterAbove, timeAbove, System.currentTimeMillis() / 1000);

                    JSONArray array = new JSONArray();

                    for (TradesItemData data : ret) {
                        JSONArray innerarray = new JSONArray();

                        innerarray.add(data.getAmount());
                        innerarray.add(data.getLastPurchaseTime());
                        innerarray.add(data.getPrice());
                        innerarray.add(data.getType().getValue());

                        array.add(innerarray);
                    }
                    final String retString = CustomXorEncryption.custom_xor_encrypt(array.toJSONString(), nonce);
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
