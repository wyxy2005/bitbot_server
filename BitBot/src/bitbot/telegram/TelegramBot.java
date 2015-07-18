package bitbot.telegram;

import bitbot.cache.tickers.ReturnVolumeProfileData;
import bitbot.cache.tickers.TickerItemData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import bitbot.util.HttpClient;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import org.apache.commons.lang3.StringEscapeUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author zheng
 */
public class TelegramBot {

    // Constats, API token
    private static final boolean IsTestDeployment = false;
    private static final String TelegramAPITestToken = "115441574:AAGFFwuS-oXEa_A4EIo_IjKNzBMRVsqUx7A";
    private static final String TelegramAPIToken = "105220285:AAGHCiRpv-4ke71o8NKcoTv7qbDHQ6YVM8M";
    private static final String[] RandomMtgoxMsg = {
        "goxcoins!",
        "goxxed!",
        "ltc on gox!",
        "Trade Engine Midas Our new trading engine is finished and soon to be deployed after a couple of hardware updates. Code-named 'Midas'",
        "karp pig ftw!",
        "Dude, you've been goxxed.",
        "Waking up and realizing your financial speculation just went south...and you should have known better.",
        "I sent money to a guy on Craig's List for a case of motor oil. He is nowhere to be found. I think I got goxxed.",
        "The act of being screwed by an online merchant, specifically due to the merchant being incompetent and amateurish. Derived from the debacle of the mtgox.com Bitcoin trading site's crash and burn and the resulting chaos trying to re-open. Poor communication, outright lies, scapegoats, Oh My! "
    };
    private static final String[] VolumeProfileTestPairsSplit = "btce-btc_usd---bitstamp-btc_usd---okcoin-btc_cny---huobi-btc_cny".split("---");
    private static final String[] VolumeProfilePairsSplit = "btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-ltc_btc---btce-ltc_usd---btcchina-btc_cny---btcchina-ltc_cny---btcchina-ltc_btc---coinbaseexchange-btc_usd---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-ltc_usd---okcoininternational-btc_usd---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-drk_usd---bitfinex-drk_btc---huobi-ltc_cny---huobi-btc_cny---cryptsy-btc_usd---cryptsy-ltc_btc---_796-btc Futures_usd---_796-ltc Futures_usd---bitvc-btc Futures Weekly_cny---bitvc-btc Futures Quarterly_cny---bitvc-btc Futures BiWeekly_cny".split("---");
    private static final String EasterEggMsg = "Oooh\n"
            + "\n"
            + "We're no strangers to love\n"
            + "You know the rules and so do I\n"
            + "A full commitment's what I'm thinking of\n"
            + "You wouldn't get this from any other guy\n"
            + "\n"
            + "I just wanna tell you how I'm feeling\n"
            + "Gotta make you understand\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you\n"
            + "\n"
            + "We've known each other for so long\n"
            + "Your heart's been aching, but you're too shy to say it\n"
            + "Inside, we both know what's been going on\n"
            + "We know the game and we're gonna play it\n"
            + "\n"
            + "And if you ask me how I'm feeling\n"
            + "Don't tell me you're too blind to see\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you\n"
            + "\n"
            + "(Ooh, give you up)\n"
            + "(Ooh, give you up)\n"
            + "Never gonna give, never gonna give\n"
            + "(Give you up)\n"
            + "Never gonna give, never gonna give\n"
            + "(Give you up)\n"
            + "\n"
            + "We've known each other for so long\n"
            + "Your heart's been aching, but you're too shy to say it\n"
            + "Inside, we both know what's been going on\n"
            + "We know the game and we're gonna play it\n"
            + "\n"
            + "I just wanna tell you how I'm feeling\n"
            + "Gotta make you understand\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you\n"
            + "\n"
            + "Never gonna give you up\n"
            + "Never gonna let you down\n"
            + "Never gonna run around and desert you\n"
            + "Never gonna make you cry\n"
            + "Never gonna say goodbye\n"
            + "Never gonna tell a lie and hurt you ";

    // Periodioc task
    private static final int PeriodicTask_RepeatTime = 5000;
    private static final int PeriodicTask_FirstDelay = 5000;
    private LoggingSaveRunnable runnable = null;
    private int PeriodicTask_MsgOffset = 0;

    // Cache
    private static String Cache_VolumeProfileSummary = null;
    private static long Cache_VolumeProfileLastUpdatedTime = 0;

    // RND
    private final Random rnd = new Random();

    // Telegram info
    private int TelegramBotId = -1;
    private String TelegramFirstName = null;
    private String TelegramUsername = null;

    // Canter troll
    private final Map<Integer, Integer> CanterList = new HashMap<>();

    // Bot state
    private TelegramBotState Telegrambotstate = TelegramBotState.Loading;
    private boolean isConnecting = false;

    public TelegramBot() {
        // Register periodic pooling task
        runnable = TimerManager.register(new TelegramPeriodicTask(), PeriodicTask_RepeatTime, PeriodicTask_FirstDelay, Integer.MAX_VALUE);
    }

    public class TelegramPeriodicTask implements Runnable {

        @Override
        public void run() {
            switch (Telegrambotstate) {
                case Loading: {
                    System.out.println("[TelegramBot] Loading...");

                    if (getMe()) {
                        Telegrambotstate = TelegramBotState.Initialized;
                    }
                    break;
                }
                case Initialized: {
                    if (!isConnecting) {
                        isConnecting = true;
                        System.out.println("[TelegramBot] Getting updates...");

                        getUpdates();

                        isConnecting = false;
                    }
                    break;
                }
            }
        }
    }

    /**
     * Gets the bot's standard profile information This is executed for the
     * first time on initialization
     *
     * @return boolean
     */
    private boolean getMe() {
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/getMe",
                IsTestDeployment ? TelegramAPITestToken : TelegramAPIToken);

        try {
            JSONObject jsonObject = (JSONObject) parseStandardTelegramJSONResponse(ConnectingURL, null);

            if (jsonObject == null) {
                return false;
            }
            // Update telegram id and username info
            TelegramBotId = Integer.parseInt(jsonObject.get("id").toString());
            TelegramFirstName = jsonObject.get("first_name").toString();
            TelegramUsername = jsonObject.get("username").toString();

            return true;
        } catch (Exception pexp) {
            pexp.printStackTrace();
        }
        return false;
    }

    /**
     * Get the latest chat updates for the bot to process
     *
     * @return boolean
     */
    private boolean getUpdates() {
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d",
                IsTestDeployment ? TelegramAPITestToken : TelegramAPIToken,
                PeriodicTask_MsgOffset);

        try {
            JSONArray jsonArray = (JSONArray) parseStandardTelegramJSONResponse(ConnectingURL, null);

            if (jsonArray == null) {
                return false;
            }
            for (Object o : jsonArray) {
                JSONObject resultObject = (JSONObject) o;

                try {
                    int updateid = Integer.parseInt(resultObject.get("update_id").toString());

                    if (PeriodicTask_MsgOffset == updateid) {
                        continue;
                    }

                    JSONObject messageObject = (JSONObject) resultObject.get("message");

                    // Is this message a new chat participants?
                    if (messageObject.containsKey("new_chat_participant")) {
                        JSONObject new_chat_participantObject = (JSONObject) messageObject.get("new_chat_participant");

                        String username = new_chat_participantObject.get("username").toString();
                        int new_chat_participantId = Integer.parseInt(new_chat_participantObject.get("id").toString());

                        final String ReplyMessage = String.format("Hi %s!! I am %s.\r\nYou can get the latest updates on the Bitcoin market with me.\r\n\r\nUse the following commands:\r\n/help", username, TelegramFirstName);

                        // Sent the message
                        sendMessage(new_chat_participantId, ReplyMessage);
                    } // Is there any text available to parse?
                    else if (messageObject.containsKey("text")) {
                        String textSaid = messageObject.get("text").toString();

                        if (textSaid.startsWith("/")) {
                            textSaid = StringEscapeUtils.unescapeJava(URLDecoder.decode(textSaid.toLowerCase(), "UTF-8")); // Update to lower case to prevent case sensitivity issue

                            // Who said the message?
                            JSONObject chat_participantObject = (JSONObject) messageObject.get("chat");
                            JSONObject from_participantObject = (JSONObject) messageObject.get("from");

                            int fromid = 0;
                            int chatid = Integer.parseInt(chat_participantObject.get("id").toString());

                            String chatUsername;
                            if (from_participantObject != null) {
                                chatUsername = from_participantObject.get("first_name").toString() + from_participantObject.get("last_name").toString();
                                fromid = Integer.parseInt(from_participantObject.get("id").toString());
                            } else {
                                chatUsername = chat_participantObject.get("title").toString();
                                fromid = chatid;
                            }

                            String[] splitText = textSaid.split(" ");
                            switch (splitText[0]) {
                                case "/help":
                                case "/start": {
                                    final String ReplyMessage = String.format("Hi %s! I am %s. \r\nYou can get the latest updates on the Bitcoin market with me."
                                            + " \r\n\r\nTo start, use the following commands: \r\n/help\r\n/summary <OPTIONAL filter> (Market price)\r\n/gox\r\n/phoneapp (Links for Bitbot mobile app)\r\n/volumeprofile (The volume profile of market buys/sells)", chatUsername, TelegramFirstName);

                                    // Sent the message
                                    sendMessage(chatid, ReplyMessage);
                                    break;
                                }
                                case "/phoneapp": {
                                    final String ReplyMessage = "Windows Phone 8.1+: http://www.windowsphone.com/en-us/store/app/bitbot-live-bitcoin-tracker/387f8f84-c5eb-436a-839a-bc1f696c7d02\r\n"
                                            + "Windows 8.1+: http://apps.microsoft.com/windows/app/bitbot/6c6e7845-f1a9-4477-9a7e-6dc9cb24a532\r\n"
                                            + "Android: Coming soon!\r\n"
                                            + "iOS: Coming soon too!!!\r\n";

                                    // Sent the message
                                    sendMessage(chatid, ReplyMessage);
                                    break;
                                }
                                case "/volumeprofile":
                                case "/volume": {
                                    final long cTime = System.currentTimeMillis();
                                    if (cTime - Cache_VolumeProfileLastUpdatedTime > 60000 || Cache_VolumeProfileSummary == null) {
                                        // String output 
                                        final StringBuilder sb = new StringBuilder("Market buy/sell profile of cryptocurrencies:\r\n\r\n");

                                        final List<Integer> returnHours = new ArrayList();
                                        returnHours.add(1);
                                        returnHours.add(24);

                                        for (String pair : (IsTestDeployment ? VolumeProfileTestPairsSplit : VolumeProfilePairsSplit)) {
                                            final String[] ExchangeCurrency = pair.split("-");
                                            final String[] CurrencySplit = ExchangeCurrency[1].split("_");

                                            final List<ReturnVolumeProfileData> profiles
                                                    = ChannelServer.getInstance().getTickerTask().getVolumeProfile(ExchangeCurrency[1], returnHours, ExchangeCurrency[0]);

                                            // 1 hour profile
                                            final ReturnVolumeProfileData profile_1h = profiles.get(0);

                                            sb.append("[").append(ExchangeCurrency[0].toUpperCase()).append(" ").append(ExchangeCurrency[1].toUpperCase().replace("_", "/")).append("]\r\n");
                                            sb.append(" -- 1H -- \r\n...Buys: ")
                                                    .append(profile_1h.TotalBuyVolume_Cur).append(", ").append(CurrencySplit[0].toUpperCase()).append("...Sells: ")
                                                    .append(profile_1h.TotalSellVolume_Cur).append(" ").append(CurrencySplit[0].toUpperCase()).append("\r\n");

                                            double totalvolume = profile_1h.TotalBuyVolume_Cur + profile_1h.TotalSellVolume_Cur;
                                            sb.append("    Ratio of BUY:SELL ( ")
                                                    .append(String.format("%.2f", profile_1h.TotalBuyVolume_Cur / totalvolume * 100)).append(" : ")
                                                    .append(String.format("%.2f", profile_1h.TotalSellVolume_Cur / totalvolume * 100)).append(" )\r\n");

                                            // 24 hour profile
                                            final ReturnVolumeProfileData profile_24h = profiles.get(1);

                                            sb.append("\r\n -- 24H -- \r\n...Buys: ")
                                                    .append(profile_24h.TotalBuyVolume_Cur).append(", ").append(CurrencySplit[0].toUpperCase()).append("...Sells: ")
                                                    .append(profile_24h.TotalSellVolume_Cur).append(" ").append(CurrencySplit[0].toUpperCase()).append("\r\n");

                                            double totalvolume24h = profile_24h.TotalBuyVolume_Cur + profile_24h.TotalSellVolume_Cur;
                                            sb.append("    Ratio of BUY:SELL ( ")
                                                    .append(String.format("%.2f", profile_24h.TotalBuyVolume_Cur / totalvolume24h * 100)).append(" : ")
                                                    .append(String.format("%.2f", profile_24h.TotalSellVolume_Cur / totalvolume24h * 100)).append(" )\r\n\r\n");
                                        }
                                        final Calendar cal = Calendar.getInstance();
                                        sb.append("\r\n -- ").append(cal.getTime().toString()).append(", ").append(cal.getTimeZone().getDisplayName());

                                        sb.append("\r\n\r\nOnly certain BTC and LTC pairs are available and in the form of 1 & 24 hours. It will be updated once every minute.\r\n");
                                        sb.append("Please download the Bitbot app from the App store for more detailed information.");

                                        Cache_VolumeProfileSummary = sb.toString();
                                        Cache_VolumeProfileLastUpdatedTime = cTime;
                                    }
                                    // Sent the message
                                    sendMessage(chatid, Cache_VolumeProfileSummary);
                                    break;
                                }
                                case "/rickroll":
                                case "/easteregg":
                                case "/christmas":
                                case "/easter": {
                                    // Sent the message
                                    sendMessage(fromid, EasterEggMsg);
                                    break;
                                }
                                case "/summary": {
                                    // User input
                                    final String UserInput_filter = splitText.length >= 2 ? splitText[1] : "";

                                    // Data
                                    final Map<String, TickerItemData> ret = ChannelServer.getInstance().getTickerTask().getExchangePriceSummaryData();
                                    final Calendar cal = Calendar.getInstance();

                                    // String output 
                                    final StringBuilder sb = new StringBuilder("Market summary of cryptocurrencies:\r\n\r\n");

                                    for (Entry<String, TickerItemData> entry : ret.entrySet()) {
                                        final TickerItemData item = entry.getValue();

                                        if (item != null && entry.getKey().contains(UserInput_filter)) {
                                            final String[] ExchangeCurrency = entry.getKey().toUpperCase().split("-");

                                            sb.append("  [").append(ExchangeCurrency[0]).append(", ");
                                            sb.append(ExchangeCurrency[1].replace("_", "/")).append("] ");

                                            sb.append(item.getClose()).append("\r\n");
                                        }
                                    }
                                    sb.append("\r\n -- ").append(cal.getTime().toString()).append(", ").append(cal.getTimeZone().getDisplayName());

                                    // Sent the message
                                    sendMessage(chatid, sb.toString());
                                    break;
                                }
                                case "/gox": {
                                    // Sent the message
                                    sendMessage(chatid, RandomMtgoxMsg[(int) Math.floor(rnd.nextInt(RandomMtgoxMsg.length))]);
                                    break;
                                }
                                case "/canter": {
                                    // chatid
                                    int canterTimes = 100;

                                    if (splitText.length >= 2) {
                                        try {
                                            canterTimes = Integer.parseInt(splitText[1]);
                                            if (canterTimes < 0 || canterTimes >= 1000) {
                                                canterTimes = 1000;
                                            }
                                        } catch (Exception exp) {
                                        }
                                    }

                                    sendMessage(chatid, "At your command! Cantering " + canterTimes + " times");

                                    /* if (!CanterList.containsKey(chatid)) {
                                     CanterList.put(chatid, canterTimes);
                                        
                                        
                                     }*/
                                    for (int i = 0; i < canterTimes; i++) {
                                        sendMessage(chatid, RandomMtgoxMsg[(int) Math.floor(rnd.nextInt(RandomMtgoxMsg.length))]);
                                    }
                                    break;
                                }
                                default: {
                                    // Sent the message
                                    sendMessage(fromid, splitText[0] + " is an unsupported command :( ");
                                    break;
                                }
                            }
                        }
                    }

                    PeriodicTask_MsgOffset = updateid;
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }

            return true;
        } catch (Exception pexp) {
            pexp.printStackTrace();
        }
        return false;
    }

    /**
     * Sends a message to the specified chatid or userid.
     *
     * @param chat_id
     * @param text
     *
     * @return boolean success or not
     */
    private boolean sendMessage(int chat_id, String text) throws Exception {
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%d&text=%s",
                IsTestDeployment ? TelegramAPITestToken : TelegramAPIToken,
                chat_id,
                URLEncoder.encode(text, "UTF-8"));

        try {
            JSONObject jsonObject = (JSONObject) parseStandardTelegramJSONResponse(ConnectingURL, null);

            if (jsonObject != null) {
                return true;
            }
        } catch (Exception pexp) {
        }
        return false;
    }

    /**
     * Parses the standard Telegram JSON response, with 'ok' and returns the
     * result JSON object.
     *
     * @param ConnectingURL
     * @return JSONObject the json object containing the response
     */
    private static Object parseStandardTelegramJSONResponse(String ConnectingURL, String postMsg) {
        String HTTPReturnData;
        if (postMsg == null) {
            HTTPReturnData = HttpClient.httpsGet(ConnectingURL, "");
        } else {
            HTTPReturnData = HttpClient.httpsPost(ConnectingURL, postMsg);
        }

        if (HTTPReturnData != null) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(HTTPReturnData);

                if (Boolean.parseBoolean(jsonObject.get("ok").toString()) == true) {
                    return jsonObject.get("result");
                }
            } catch (Exception pexp) {
                pexp.printStackTrace();
            }
        }
        return null;
    }

    private void disposeInstance() {
        if (runnable != null) {
            runnable.getSchedule().cancel(false);
            runnable = null;
        }
    }

    private enum TelegramBotState {

        Loading(0),
        Initialized(1);

        private final int state;

        private TelegramBotState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
