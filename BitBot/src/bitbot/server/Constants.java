package bitbot.server;

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
    public static final String DefaultCurrencyPair = "btce-btc_usd---btce-ltc_usd---btce-ftc_btc---btce-xpm_btc---btce-trc_btc---btce-nvc_usd---btce-ppc_usd---btce-nmc_usd---btcchina-btc_cny---mtgox-btc_usd";
    
    // server-client
    public static final boolean enableUnhandledPacketLogging = false;
    public static final short ServerClientVersion = 1;
    public static final String ServerClientMinorPatchVer = "0";
    public static final String CLIENT_KEY = "CLIENT";
    
    // server-server
    public static final long PriceBetweenServerBroadcastDelay = 2000;

}
