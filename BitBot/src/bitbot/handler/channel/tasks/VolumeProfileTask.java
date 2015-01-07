package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.ReturnVolumeProfileData;
import bitbot.handler.channel.ChannelServer;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
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
public class VolumeProfileTask implements Runnable {

    private final Response response;
    private final Request request;

    private final List<Integer> hoursFromNow;
    private final String ExchangeSite;
    private final String currencypair, serverAuthorization;
    private final long nonce;
    private final int APIVersion;

    private boolean isAuthorized = true;

    public VolumeProfileTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;

        this.nonce = Long.parseLong(query.get("nonce"));
        final long cTime = System.currentTimeMillis();
        if (cTime - (60 * 60 * 24 * 1000) > nonce || cTime + (60 * 60 * 24 * 1000) < nonce) {
            isAuthorized = false;
        }

        // hours
        hoursFromNow = new ArrayList();
        String strhoursFromNow = query.get("hoursFromNow");
        String[] strhoursFromNowArray = strhoursFromNow.split(",");
        for (String hours : strhoursFromNowArray) {
            int val = 0;
            try {
                val = Integer.parseInt(hours);
            } catch (Exception exp) {
            }

            if (val <= 0 || val > (24 * 7 * 4)) { // don't allow selection for more than a 4 weeks
                isAuthorized = false;
            } else {
                hoursFromNow.add(Integer.parseInt(hours));
            }
        }
        if (hoursFromNow.isEmpty() || hoursFromNow.size() > 7) { // max 7 items, to prevent denial of service
            isAuthorized = false;
        }

        // exchange site
        if (query.containsKey("ExchangeSite")) {
            ExchangeSite = query.get("ExchangeSite");
        } else {
            ExchangeSite = "btce";
        }

        // currency pair
        this.currencypair = query.get("currencypair");

        // API Version
        if (query.containsKey("APIVersion")) {
            APIVersion = Integer.parseInt(query.get("APIVersion"));
        } else {
            APIVersion = 1;
            isAuthorized = false;
        }

        // checks
        this.serverAuthorization = query.get("serverAuthorization").replace(' ', '+');

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), strhoursFromNow + currencypair + (0x1000000 ^ nonce) + ExchangeSite);
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
                    List<ReturnVolumeProfileData> profiles = ChannelServer.getInstance().getTickerTask().getVolumeProfile(currencypair, hoursFromNow, ExchangeSite);

                    JSONArray array = new JSONArray();

                    for (ReturnVolumeProfileData profile : profiles) {
                        JSONArray obj_array = new JSONArray();
                        
                        obj_array.add(profile.TotalBuyVolume);
                        obj_array.add(profile.TotalSellVolume);
                        obj_array.add(profile.TotalBuyVolume_Cur);
                        obj_array.add(profile.TotalSellVolume_Cur);

                        if (profile.TotalBuyVolume > profile.TotalSellVolume) { // buy is more than sell
                            obj_array.add(1);
                            obj_array.add(profile.TotalSellVolume / profile.TotalBuyVolume);
                        } else { // sell is more than buy
                            obj_array.add(profile.TotalBuyVolume / profile.TotalSellVolume);
                            obj_array.add(1);
                        }
                        
                        array.add(obj_array);
                    }

                    body.print(
                            CustomXorEncryption.custom_xor_encrypt(array.toJSONString(), nonce)
                    );
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
