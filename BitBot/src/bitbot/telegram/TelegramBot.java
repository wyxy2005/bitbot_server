package bitbot.telegram;

import bitbot.cache.tickers.TickerItemData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import bitbot.util.HttpClient;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
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
    private static final String TelegramAPIToken = "105220285:AAGHCiRpv-4ke71o8NKcoTv7qbDHQ6YVM8M";
    private static final String[] RandomMtgoxMsg = {
        "goxcoins!", 
        "goxxed!", 
        "ltc on gox!", 
        "Trade Engine Midas Our new trading engine is finished and soon to be deployed after a couple of hardware updates. Code-named 'Midas'",
        "karp pig ftw!"};
    
    // Periodioc task
    private static final int PeriodicTask_RepeatTime = 5000;
    private static final int PeriodicTask_FirstDelay = 5000;
    private LoggingSaveRunnable runnable = null;
    private int PeriodicTask_MsgOffset = 0;
    
    // RND
    private Random rnd = new Random();

    // Telegram info
    private int TelegramBotId = -1;
    private String TelegramFirstName = null;
    private String TelegramUsername = null;

    // Bot state
    private TelegramBotState Telegrambotstate = TelegramBotState.Loading;

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
                    System.out.println("[TelegramBot] Getting updates...");

                    getUpdates();
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
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/getMe", TelegramAPIToken);

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
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d", TelegramAPIToken, PeriodicTask_MsgOffset);

        try {
            JSONArray jsonArray = (JSONArray) parseStandardTelegramJSONResponse(ConnectingURL, null);

            if (jsonArray == null) {
                return false;
            }
            for (Object o : jsonArray) {
                JSONObject resultObject = (JSONObject) o;

                try {
                    int updateid = Integer.parseInt(resultObject.get("update_id").toString());
                    
                    if (PeriodicTask_MsgOffset == updateid)
                        continue;
                    
                    JSONObject messageObject = (JSONObject) resultObject.get("message");

                    // Is this message a new chat participants?
                    if (messageObject.containsKey("new_chat_participant")) {
                        JSONObject new_chat_participantObject = (JSONObject) messageObject.get("new_chat_participant");

                        String username = new_chat_participantObject.get("username").toString();
                        int new_chat_participantId = Integer.parseInt(new_chat_participantObject.get("id").toString());

                        final String ReplyMessage = String.format("Hi %s. You can get the latest updates on the Bitcoin market with me.\r\n\r\nUse the following commands:\r\n/help", username);

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
                                    final String ReplyMessage = String.format("Hi %s. \r\nYou can get the latest updates on the Bitcoin market with me."
                                            + " \r\n\r\nTo start, use the following commands: \r\n/help\r\n/summary <OPTIONAL filter>\r\n/gox", chatUsername);

                                    // Sent the message
                                    sendMessage(chatid, ReplyMessage);
                                    break;
                                }
                                case "/summary": {
                                    final Map<String, TickerItemData> ret = ChannelServer.getInstance().getTickerTask().getExchangePriceSummaryData();
                                    final Calendar cal = Calendar.getInstance();
                                    String dateFormattedStr = String.format("%d:%d %d.%d.%d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
                                    
                                    String filter = splitText.length >= 2 ? splitText[1] : "";
                                    
                                    final StringBuilder sb = new StringBuilder("Market summary of cryptocurrencies as of ").append(dateFormattedStr).append(":\r\n");
                                    for (Entry<String, TickerItemData> entry : ret.entrySet()) {
                                        TickerItemData item = entry.getValue();
                                        
                                        if (entry.getKey().contains(filter)) {
                                            sb.append("[").append(entry.getKey()).append("] ").append(item.getClose()).append("\r\n");
                                        }
                                    }
                                    
                                    // Sent the message
                                    sendMessage(chatid, sb.toString());
                                    break;
                                }
                                case "/gox": {
                                    // Sent the message
                                    sendMessage(chatid, RandomMtgoxMsg[(int) Math.floor(rnd.nextInt(RandomMtgoxMsg.length))]);
                                    break;
                                }
                                default: {
                                    // Sent the message
                                    sendMessage(fromid, splitText[0] +" is an unsupported command :( ");
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
        final String ConnectingURL = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%d&text=%s", TelegramAPIToken, chat_id, URLEncoder.encode(text, "UTF-8"));

        try {
            JSONObject jsonObject = (JSONObject) parseStandardTelegramJSONResponse(ConnectingURL, null);

            if (jsonObject == null)
                return false;
            
            return true;
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
