package bitbot;

/**
 *
 * @author z
 */
public class Constants {
    
    public static final String Server_UserAgent = "BitBot/1.0";
    public static final String Server_UserAgentAzure = "BitBot/1.0 - WindowsAzure -;4.2";
    public static final String Server_AzureAuthorization = "a's;d035o3-45034,fgijdmgld-=2";
    public static final String Azure_X_ZUMO_APPLICATION = "SuYSMlztVWyxmrOrIcgNeiTtFkiFYd36";
    
    public static final String IE11_UserAgent = "Mozilla/5.0 (Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko";
    
    /// etc
    
    public static final String CurrencyPairFile = "CurrencyPairs.properties";
    public static final String CurrencyPairSwapsFile = "CurrencyPairs_swaps.properties";

    // server-client
    public static final boolean enableUnhandledPacketLogging = false;
    public static final short ServerClientVersion = 1;
    public static final String ServerClientMinorPatchVer = "0";
    public static final String CLIENT_KEY = "CLIENT";
    
    // server-server
    public static final long PriceBetweenServerBroadcastDelay = 9;
    
    // Socket
    public static final int SocketPortLegacy = 8080; // to provide compatibility for those on old version of the app
    public static final int SocketPort_Stream = 8082;
    
    // Logging email warnings
    public static final String Logging_EmailFrom = "bitbotlive@outlook.com";
    public static final String Logging_EmailTo = "twilight-ofthepast@live.com";
    public static final String[] SendGridCredentials_User = {"azure_e61e429d81c4fef32ec4cbcbc312178b@azure.com", "azure_181d59c8dd3b490c0743d275a9ce272b@azure.com"};
    public static final String[] SendGridCredentials_Password = {"5Jp4w8eCoFaW18w", "8SvQzlIZqoxd4EF"};
    
    // TradingView UDF
    public static boolean tv_supports_search = true;
    public static boolean tv_supports_group_request = false;
    public static boolean tv_supports_marks = false;
    
    public static String[] tv_exchange_value = { "",                "BTCe", "Bitfinex", "Bitstamp", "Okcoin", "BTCChina", "Coinbase", "CoinbaseExchange", "Campbx", "Itbit", "Cryptsy", "_796", "Fybsg", "Fybse", "Kraken", "CexIO", "Dgex", "BitVC", "Gemini"};
    public static String[] tv_exchange_name = { "All Exchanges",    "BTCe", "Bitfinex", "Bitstamp", "Okcoin", "BTCChina", "Coinbase", "CoinbaseExchange", "Campbx", "Itbit", "Cryptsy", "_796", "Fybsg", "Fybse", "Kraken", "CexIO", "Dgex", "BitVC", "Gemini"};
    public static String[] tv_exchange_desc = { "",                 "BTCe", "Bitfinex", "Bitstamp", "Okcoin", "BTCChina", "Coinbase", "CoinbaseExchange", "Campbx", "Itbit", "Cryptsy", "_796", "Fybsg", "Fybse", "Kraken", "CexIO", "Dgex", "BitVC", "Gemini"};
    

    public static String[] tv_symbolType1 = { "All types", "Bitcoin", "Stock", "Index"};
    public static String[] tv_symbolType2 = { "", "bitcoin", "stock", "index"};
    
    public static String[] tv_supportedResolutions = { "1", "3", "5", "10", "15", "30", "60", "120", "240", "360", "720", "1D", "2D", "3D", "W", "2W", "3W", "1M"};
}