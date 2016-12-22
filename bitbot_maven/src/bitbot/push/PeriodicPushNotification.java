package bitbot.push;

import bitbot.handler.world.WorldChannelInterfaceImpl;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import com.windowsazure.messaging.NamespaceManager;
import com.windowsazure.messaging.Notification;
import com.windowsazure.messaging.NotificationHub;
import com.windowsazure.messaging.NotificationHubDescription;
import com.windowsazure.messaging.NotificationHubsException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author zhenghao
 */
public class PeriodicPushNotification {

    private static final String SENDERID = "62212154532";
    private static final String HUBNAME = "bitcoinbothub";//"bitcoinbothub-ns";
    private static final String CONNECTION_STRING = "Endpoint=sb://bitcoinbothub-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultFullSharedAccessSignature;SharedAccessKey=lZPD5jA09ql3hDpS8lNnQRlK3DMZ2emRSHXcTb2NwtI=";

    private static final NamespaceManager NAMESPACE = new NamespaceManager(CONNECTION_STRING);

    private static PeriodicPushNotification INSTANCE;

    private static NotificationHub hub;
    private static NotificationHubDescription hubDesc;

    public static PeriodicPushNotification getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PeriodicPushNotification();

            try {
                hub = new NotificationHub(CONNECTION_STRING, HUBNAME); // Run this line.
                hubDesc = NAMESPACE.getNotificationHub(HUBNAME);
            } catch (NotificationHubsException exp) {
                exp.printStackTrace();

                throw new RuntimeException("Error initializing notification hub."); // rethrow... cant do much about this crap
            }
        }
        return INSTANCE;
    }

    private static final String[] USDPairsIndex = {"bitfinex-btc_usd", "bitstamp-btc_usd", "btce-btc_usd", "gemini-btc_usd", "itbit-xbt_usd", "okcoininternational-btc_usd"};
    private static final String[] CNYPairsIndex = {"okcoin-btc_cny", "btcchina-btc_cny", "huobi-btc_cny"};
    public void sendAndroidDailyNotification() {
        double avgBTCUSDPrice = 0;
        int addedBTCCount = 0;
        for (String s : USDPairsIndex) {
            double price = WorldChannelInterfaceImpl.getInstantSpotPriceS(s);
            if (price != 0) {
                avgBTCUSDPrice += price;
                addedBTCCount ++;
            }
        }
        avgBTCUSDPrice /= addedBTCCount;
        
        double avgBTCCNYPrice = 0;
        int addedBTCCNYCount = 0;
        for (String s : CNYPairsIndex) {
            double price = WorldChannelInterfaceImpl.getInstantSpotPriceS(s);
            if (price != 0) {
                avgBTCCNYPrice += price;
                addedBTCCNYCount ++;
            }
        }
        avgBTCCNYPrice /= addedBTCCNYCount;
        
        final Map<String, String> prop = new HashMap<>(); // json property
        prop.put("title", "Daily price updates");
        prop.put("message", String.format("BTCUSD: $%f, BTCCNY: ¥%f", avgBTCUSDPrice, avgBTCCNYPrice));
        prop.put("type", "main"); // main, detailed, fullchart
        //prop.put("navigateTitle", CurrencyPair);
        //prop.put("navigateExchange", Exchange);
        
        /*
        {"data":{
    "title":"Hello!",
    "message":"Notification Hub test",

    "type":"detailed",

    "navigateTitle":"BTC_USD",
    "navigateExchange":"Bitfinex"}}
         */

        // The tags to send to 
        Set<String> tags = new HashSet<>();
        tags.add("android");
        //tags.add("tester");

        try {
            final Notification n = Notification.createGcmNotifiation(buildJsonString(prop));
            
            hub.sendNotification(n, tags);
        } catch (NotificationHubsException exp) {
            exp.printStackTrace();

            ServerLog.RegisterForLoggingException(ServerLogType.PushNotification, exp);
        }
    }
    
    private static String buildJsonString(Map<String, String> prop) {
        final StringBuilder buf = new StringBuilder();
        buf.append("{\"data\":{");
        for (Iterator<String> iterator = prop.keySet().iterator(); iterator
                .hasNext();) {
            String key = iterator.next();
            buf.append("\"").append(key).append("\":\"").append(prop.get(key)).append("\"");
            
            if (iterator.hasNext()) {
                buf.append(",");
            }
        }
        buf.append("}}");
        
        return buf.toString();
    }
}
