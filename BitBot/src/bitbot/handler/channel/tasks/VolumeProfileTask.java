package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.ReturnVolumeProfileData;
import bitbot.handler.channel.ChannelServer;
import bitbot.util.encryption.CustomXorEncryption;
import bitbot.util.encryption.HMACSHA1;
import java.io.IOException;
import java.io.PrintStream;
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

    private final int minutesBackFromNow;
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
        int minutesBackFromNow_ = Integer.parseInt(query.get("minutesBackFromNow"));
        if (minutesBackFromNow_ <= 0 || minutesBackFromNow_ >= (24 * 30 * 12 * 10)) {
            isAuthorized = false;
        }
        this.minutesBackFromNow = minutesBackFromNow_;

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

        final String encoded = HMACSHA1.encode(String.valueOf(nonce), currencypair + ((minutesBackFromNow ^ 0x1000000) ^ nonce) + ExchangeSite);
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
                    ReturnVolumeProfileData profile = ChannelServer.getInstance().getTickerTask().getVolumeProfile(currencypair, minutesBackFromNow, ExchangeSite);

                    JSONArray array = new JSONArray();
                    
                    array.add(profile.TotalBuyVolume);
                    array.add(profile.TotalSellVolume);
                    array.add(profile.TotalBuyVolume_Cur);
                    array.add(profile.TotalSellVolume_Cur);
                    
                    if (profile.TotalBuyVolume > profile.TotalSellVolume) { // buy is more than sell
                        array.add(1);
                        array.add(profile.TotalSellVolume / profile.TotalBuyVolume);
                    } else { // sell is more than buy
                        array.add(profile.TotalBuyVolume / profile.TotalSellVolume);
                        array.add(1);
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
