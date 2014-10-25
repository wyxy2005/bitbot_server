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
    public static final String CurrencyPairSwapsFile = "CurrencyPairs_swaps.properties";

    // server-client
    public static final boolean enableUnhandledPacketLogging = false;
    public static final short ServerClientVersion = 1;
    public static final String ServerClientMinorPatchVer = "0";
    public static final String CLIENT_KEY = "CLIENT";
    
    // server-server
    public static final long PriceBetweenServerBroadcastDelay = 2000;
    
    // Socket
    public static final int SocketPortLegacy = 8080; // to provide compatibility for those on old version of the app
    public static final int SocketPort_Stream = 8082;

}
