package bitbot.push;

import bitbot.handler.world.WorldChannelInterfaceImpl;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.server.threads.MultiThreadExecutor;
import com.windowsazure.messaging.NamespaceManager;
import com.windowsazure.messaging.Notification;
import com.windowsazure.messaging.NotificationHub;
import com.windowsazure.messaging.NotificationHubDescription;
import com.windowsazure.messaging.NotificationHubsException;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONObject;

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

    private static boolean TESTER = false;

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

    private static final String[] USD_PAIRS_INDEX_LIST = {"bitfinex-btc_usd", "bitstamp-btc_usd", "btce-btc_usd", "gemini-btc_usd", "itbit-xbt_usd", "okcoininternational-btc_usd"};
    private static final String[] CNY_PAIRS_INDEX_LIST = {"okcoin-btc_cny", "btcchina-btc_cny", "huobi-btc_cny"};

    private static double getBitcoinIndexPrice(String[] pairsList) {
        double avgBTCUSDPrice = 0;
        int addedBTCCount = 0;
        for (String s : pairsList) {
            double price = WorldChannelInterfaceImpl.getInstantSpotPriceS(s);
            if (price != 0) {
                avgBTCUSDPrice += price;
                addedBTCCount++;
            }
        }
        avgBTCUSDPrice /= addedBTCCount;

        return avgBTCUSDPrice;
    }

    /**
     * Sends the daily push notification to all mobile devices registered in the
     * Azure Notification Hub system
     */
    public void sendDailyPushNotification() {
        final double avgBTCUSDPrice = getBitcoinIndexPrice(USD_PAIRS_INDEX_LIST);
        final double avgBTCCNYPrice = getBitcoinIndexPrice(CNY_PAIRS_INDEX_LIST);

        final String message = String.format("BTCUSD: $%.2f, BTCCNY: ¥%.2f", avgBTCUSDPrice, avgBTCCNYPrice);
        final String title = "Daily price updates";

        // Android
        MultiThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendAndroidDailyPushNotification(title, message);
            }
        });
        // Windows
        MultiThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendWindowsDailyPushNotification(title, message);
            }
        });
        // iOS
        MultiThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                sendiOSDailyPushNotification(title, message);
            }
        });
    }

    /**
     * Sends the daily push notification to all iOS devices
     *
     * @param title
     * @param message
     */
    public void sendiOSDailyPushNotification(String title, String message) {
        // The tags to send to 
        Set<String> tags = new HashSet<>();
        if (!TESTER) {
            tags.add("ios");
        } else {
            tags.add("tester");
        }
        try {
            final Notification n = Notification.createAppleNotifiation(message);

            hub.sendNotification(n, tags);
        } catch (NotificationHubsException exp) {
            exp.printStackTrace();

            ServerLog.RegisterForLoggingException(ServerLogType.PushNotification, exp);
        }
    }

    /**
     * Sends the daily push notification to all Windows devices
     *
     * @param title
     * @param message
     */
    public void sendWindowsDailyPushNotification(String title, String message) {
        // The tags to send to 
        Set<String> tags = new HashSet<>();
        if (!TESTER) {
            tags.add("windows");
        } else {
            tags.add("tester");
        }
        try {
            final Notification n = Notification.createWindowsNotification(message);

            hub.sendNotification(n, tags);
        } catch (NotificationHubsException exp) {
            exp.printStackTrace();

            ServerLog.RegisterForLoggingException(ServerLogType.PushNotification, exp);
        }
    }

    /**
     * Sends the daily push notification to all Android devices
     *
     * @param title
     * @param message
     */
    public void sendAndroidDailyPushNotification(String title, String message) {
        JSONObject obj = new JSONObject(); // main json object for google gcn push
        JSONObject json_gcnPushData = new JSONObject();
        obj.put("data", json_gcnPushData);

        json_gcnPushData.put("title", title);
        json_gcnPushData.put("message", message);
        json_gcnPushData.put("type", "main"); // main, detailed, fullchart
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
        if (!TESTER) {
            tags.add("android");
        } else {
            tags.add("tester");
        }
        try {
            final Notification n = Notification.createGcmNotifiation(obj.toJSONString());

            hub.sendNotification(n, tags);
        } catch (NotificationHubsException exp) {
            exp.printStackTrace();

            ServerLog.RegisterForLoggingException(ServerLogType.PushNotification, exp);
        }
    }
}
