package bitbot.handler.channel;

import bitbot.handler.ServerHTTPExchangeHandler;
import bitbot.Constants;
import bitbot.cache.news.NewsCacheTask;
import bitbot.cache.swaps.BacklogCommitTask_Swaps;
import bitbot.cache.swaps.SwapsCacheTask;
import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.BacklogCommitTask_Tickers;
import bitbot.cache.tickers.BacklogCommitTask_Trades;
import bitbot.cache.trades.TradesCacheTask;
import bitbot.handler.ServerSocketExchangeHandler;
import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.world.WorldRegistry;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.social.telegram.TelegramBot;
import bitbot.util.encryption.SHA256;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import org.apache.mina.transport.socket.SocketAcceptor;

/**
 *
 * @author z
 */
public class ChannelServer {

    public static final ChannelServer ch = new ChannelServer();
    private ChannelWorldInterface cwi;
    private WorldChannelInterface wci = null;
    private static WorldRegistry worldRegistry;

    private Boolean worldReady = true;
    private boolean isShutdown = false,
            finishedShutdown = false,
            isReconnectState = false;

    private ServerHTTPExchangeHandler serverExchangeHandler = null;

    private SocketAcceptor acceptor;
    private InetSocketAddress InetSocketadd;

    private TickerCacheTask tickerTask = null;
    private SwapsCacheTask swapTask = null;
    private NewsCacheTask newsTask = null;
    private TradesCacheTask tradesTask = null;
    private TelegramBot telegramBotTask = null;

    private ServerSocketExchangeHandler serverSocketExchangeHandler = null;

    // Properties
    private static Properties props = null;
    private static boolean Props_EnforceCloudFlareNetwork = false,
            Props_EnableTickerHistoryDatabaseCommit = false,
            Props_EnableTickerHistory = false,
            Props_EnableSQLDataAcquisition = false,
            Props_EnableSwapsDatabaseCommit = false,
            Props_EnableSwaps = false,
            Props_EnableSwapsSQLDataAcquisition = false,
            Props_EnableSocketStreaming = false,
            Props_EnableDebugSessionPrints = false,
            Props_EnableTradesDatabaseCommit = false,
            Props_EnableTradesSQLDataAcquisition = false,
            Props_EnableTelegramBot = false;
    private static String Props_WorldIPAddress = "127.0.0.1",
            Props_SelfIPAddress = "127.0.0.1",
            Props_WorldRMIHash = "";
    private static int Props_RequiredTradeSizeForLogging = 10;
    private static short Props_WorldRMIPort = 22155,
            Props_HTTPPort = 80, Props_HTTPsPort = 443;
    private static String 
            Props_CurrencyPairsFile,
            Props_CurrencyPairsSwapsFile;
    private static final List<String> Props_CurrencyPairsForLargeTrades = new ArrayList();

    // Etc
    private final List<String> CachingCurrencyPair = new ArrayList();
    private final List<String> CachingSwapCurrencyPair = new ArrayList();

    private ChannelServer() {

    }

    public static final WorldRegistry getWorldRegistry() {
        return worldRegistry;
    }

    public static ChannelServer getInstance() {
        return ch;
    }

    public static void initializeChannelServer(String serverPropertyFilePath) {
        if (ch.serverExchangeHandler == null) {
            try {
                if (System.getSecurityManager() == null) {
                    System.setSecurityManager(new RMISecurityManager() {

                        /*		    @Override
                         public void checkConnect(String host, int port) {
                         }

                         @Override
                         public void checkConnect(String host, int port, Object context) {
                         }*/
                    });
                    System.out.println("[Info] Set SecurityManager as RMISecurityManager");
                }

                // Init properties
                System.out.println("[Info] Loading server properties..");

                if (props == null) {
                    props = new Properties();
                    try (FileReader is = new FileReader(serverPropertyFilePath)) {
                        props.load(is);
                    }
                }
                Props_EnforceCloudFlareNetwork = Boolean.parseBoolean(props.getProperty("server.EnforceCloudFlareNetwork"));

                Props_EnableTickerHistory = Boolean.parseBoolean(props.getProperty("server.EnableTickerHistory"));
                Props_EnableTickerHistoryDatabaseCommit = Boolean.parseBoolean(props.getProperty("server.EnableTickerHistoryDatabaseCommit"));
                Props_EnableSQLDataAcquisition = Boolean.parseBoolean(props.getProperty("server.EnableSQLDataAcquisition"));

                Props_EnableSwapsDatabaseCommit = Boolean.parseBoolean(props.getProperty("server.EnableSwapsDatabaseCommit"));
                Props_EnableSwaps = Boolean.parseBoolean(props.getProperty("server.EnableSwaps"));
                Props_EnableSwapsSQLDataAcquisition = Boolean.parseBoolean(props.getProperty("server.EnableSwapsSQLDataAcquisition"));

                Props_EnableTelegramBot = Boolean.parseBoolean(props.getProperty("server.EnableTelegramBot"));
                
                Props_EnableTradesSQLDataAcquisition = Boolean.parseBoolean(props.getProperty("server.EnableTradesSQLDataAcquisition"));
                Props_EnableTradesDatabaseCommit = Boolean.parseBoolean(props.getProperty("server.EnableTradesDatabaseCommit"));

                Props_SelfIPAddress = props.getProperty("server.SelfIPAddress");
                Props_EnableSocketStreaming = Boolean.parseBoolean(props.getProperty("server.EnableSocketStreaming"));
                Props_EnableDebugSessionPrints = Boolean.parseBoolean(props.getProperty("server.EnableDebugSessionPrints"));
                Props_WorldIPAddress = props.getProperty("server.WorldIPAddress");
                Props_WorldRMIPort = Short.parseShort(props.getProperty("server.WorldRMIPort"));
                Props_WorldRMIHash = SHA256.sha256(SHA256.sha256(props.getProperty("server.WorldRMIHash")));
                Props_HTTPPort = Short.parseShort(props.getProperty("server.HTTPPort"));
                Props_HTTPsPort = Short.parseShort(props.getProperty("server.HTTPsPort"));
                
                Props_CurrencyPairsFile = props.getProperty("server.CurrencyPairsFile");
                Props_CurrencyPairsSwapsFile = props.getProperty("server.CurrencyPairsSwapsFile");
                
                final String[] CurrencyPairsForLargeTrades = props.getProperty("server.trackTrades").split("---");
                for (String str : CurrencyPairsForLargeTrades) {
                    Props_CurrencyPairsForLargeTrades.add(str);
                }
                Props_RequiredTradeSizeForLogging = Integer.parseInt(props.getProperty("server.RequiredTradeSizeForLogging"));

                // Establish RMI connection
                System.out.println(String.format("[Info] Locating world server RMI connection at %s:%d..", Props_WorldIPAddress, Props_WorldRMIPort));

                final Registry registry = LocateRegistry.getRegistry(Props_WorldIPAddress, Props_WorldRMIPort/*, new SslRMIClientSocketFactory()*/);
                worldRegistry = (WorldRegistry) registry.lookup(Constants.Server_AzureAuthorization);
                ch.cwi = new ChannelWorldInterfaceImpl(ch);
                ch.wci = worldRegistry.registerChannelServer(Props_WorldRMIHash, ch.cwi, false);

                // End
                System.out.println("[Info] Loading tasks..");
                if (Props_HTTPPort != 0 || Props_HTTPsPort != 0)
                    ch.serverExchangeHandler = ServerHTTPExchangeHandler.Connect(Props_HTTPPort, Props_HTTPsPort);

                ch.LoadCurrencyPairTables(false);

                ch.tickerTask = new TickerCacheTask(); // init automatically
                ch.tradesTask = new TradesCacheTask();
                if (Props_EnableTelegramBot) {
                    ch.telegramBotTask = new TelegramBot();
                }
                ch.newsTask = new NewsCacheTask();
                ch.swapTask = new SwapsCacheTask();

                if (Props_EnableSocketStreaming) {
                    System.out.println("[Info] Loading sockets..");
                    ch.serverSocketExchangeHandler = ServerSocketExchangeHandler.connect(Props_SelfIPAddress);
                }

                // Shutdown hooks
                System.out.println("[Info] Registering shutdown hooks");
                Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
            } catch (Exception exp) {
                System.out.println("[Warning] Failed to start Channel server, please ensure the port is unused.");
                exp.printStackTrace();

                if (ch.serverExchangeHandler != null) {
                    ch.serverExchangeHandler.Disconnect();
                }
                ch.serverExchangeHandler = null;
            }
        }
    }

    public final void reconnectWorld(final Throwable e) {
        if (e != null) {
            ServerLog.RegisterForLoggingException(ServerLogType.ReconnectError, e);
        }
        try {
            wci.isAvailable();
        } catch (RemoteException ex) {
            synchronized (worldReady) {
                worldReady = false;
            }
            synchronized (cwi) {
                synchronized (worldReady) {
                    if (worldReady) {
                        return;
                    }
                }
                System.out.println("Reconnecting to world server");
                synchronized (wci) {
                    isReconnectState = true;

                    // completely re-establish the rmi connection
                    ChannelWorldInterface cwi2 = null;
                    WorldChannelInterface wci2 = null;
                    WorldRegistry reg = null;

                    boolean success = false;

                    try {
                        // initialProp.getProperty("net.sf.odinms.world.host")
                        final Registry registry = LocateRegistry.getRegistry(Props_WorldIPAddress, Props_WorldRMIPort, new SslRMIClientSocketFactory());
                        reg = (WorldRegistry) registry.lookup(Constants.Server_AzureAuthorization);
                        cwi2 = new ChannelWorldInterfaceImpl(this);
                        wci2 = reg.registerChannelServer(Props_WorldRMIHash, cwi2, true);

                        wci2.serverReady();

                        this.finishedShutdown = false;
                        this.isShutdown = false;

                        success = true;
                    } catch (Exception exs) {
                        System.err.println("Reconnecting failed" + exs);

                        ServerLog.RegisterForLoggingException(ServerLogType.ReconnectError, exs);
                    }
                    if (success) {
                        worldRegistry = reg;
                        cwi = cwi2;
                        wci = wci2;

                        System.err.println("Reconnecting [Channel] to [World] success");
                    }
                    isReconnectState = false;
                    worldReady = true;
                }
            }
            synchronized (worldReady) {
                worldReady.notifyAll();
            }
        }
    }

    public void LoadCurrencyPairTables(boolean reload) {
        if (!CachingCurrencyPair.isEmpty() && !reload) {
            return;
        }

        // Currency pairs
        File f = new File(Props_CurrencyPairsFile);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ioe) {
                System.out.println(ioe.toString() + " Error accessing file system to read currency pairs");
            }
        }
        try (FileReader Reader = new FileReader(Props_CurrencyPairsFile)) {
            try (BufferedReader bufReader = new BufferedReader(Reader)) {
                String str = bufReader.readLine();

                if (str != null) {
                    String[] SourcePairs = str.split("---");
                    CachingCurrencyPair.clear();

                    for (String SourcePair : SourcePairs) {
                        CachingCurrencyPair.add(SourcePair);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString() + " Error accessing file system to read currency pairs");
        }

        // Swap currency pairs
        File f2 = new File(Props_CurrencyPairsSwapsFile);
        if (!f2.exists()) {
            try {
                f2.createNewFile();
            } catch (IOException ioe) {
                System.out.println(ioe.toString() + " Error accessing file system to read swap currency pairs");
            }
        }
        try (FileReader Reader = new FileReader(Props_CurrencyPairsSwapsFile)) {
            try (BufferedReader bufReader = new BufferedReader(Reader)) {
                String str = bufReader.readLine();

                if (str != null) {
                    String[] SourcePairs = str.split("---");
                    CachingSwapCurrencyPair.clear();

                    for (String SourcePair : SourcePairs) {
                        CachingSwapCurrencyPair.add(SourcePair);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString() + " Error accessing file system to read swap currency pairs");
        }

    }

    public int getRequiredTradeSizeForTradesLogging() {
        return Props_RequiredTradeSizeForLogging;
    }

    public List<String> getCurrencyPairsForLargeTrades() {
        return Props_CurrencyPairsForLargeTrades;
    }

    public List<String> getCachingCurrencyPair() {
        return CachingCurrencyPair;
    }

    public List<String> getCachingSwapCurrencies() {
        return CachingSwapCurrencyPair;
    }

    public ServerSocketExchangeHandler getServerSocketExchangeHandler() {
        return serverSocketExchangeHandler;
    }

    public TradesCacheTask getTradesTask() {
        return tradesTask;
    }
    
    public TickerCacheTask getTickerTask() {
        return tickerTask;
    }

    public NewsCacheTask getNewsTask() {
        return newsTask;
    }

    public SwapsCacheTask getSwapsTask() {
        return swapTask;
    }
    
    public TelegramBot getTelegramBotTask(){
        return telegramBotTask;
    }

    public boolean isEnforceCloudFlareNetwork() {
        return Props_EnforceCloudFlareNetwork;
    }

    public boolean isEnableTickerHistoryDatabaseCommit() {
        return Props_EnableTickerHistoryDatabaseCommit;
    }

    public boolean isEnableTickerHistory() {
        return Props_EnableTickerHistory;
    }

    public boolean isEnableSQLDataAcquisition() {
        return Props_EnableSQLDataAcquisition;
    }

    public boolean isEnableEnableSwapsDatabaseCommit() {
        return Props_EnableSwapsDatabaseCommit;
    }

    public boolean isEnableEnableSwaps() {
        return Props_EnableSwaps;
    }

    public boolean isEnableSwapsSQLDataAcquisition() {
        return Props_EnableSwapsSQLDataAcquisition;
    }

    public boolean isEnableSocketStreaming() {
        return Props_EnableSocketStreaming;
    }

    public boolean isEnableDebugSessionPrints() {
        return Props_EnableDebugSessionPrints;
    }

    public boolean isEnableTradesDatabaseCommit() {
        return Props_EnableTradesDatabaseCommit;
    }

    public boolean isEnableTradesSQLDataAcquisition() {
        return Props_EnableTradesSQLDataAcquisition;
    }

    public boolean isShutdown() {
        return isShutdown;
    }

    public final WorldChannelInterface getWorldInterface() {
        synchronized (worldReady) {
            while (!worldReady) {
                try {
                    worldReady.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        return wci;
    }

    public void broadcastPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) {
        try {
            wci.broadcastPriceChanges(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio, last);
        } catch (RemoteException exp) {
            ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);

            // attempt reconnect
            reconnectWorld(null);
        } catch (NoClassDefFoundError servError) {
            // world server may have crashed or inactive :(
            System.out.println("[Warning] World Server may be inacctive or crashed. Please restart.");
            servError.printStackTrace();
        }
    }

    public void broadcastSwapData(String ExchangeCurrency, float rate, float spot_price, double amount_lent, int timestamp) {
        try {
            wci.broadcastSwapData(ExchangeCurrency, rate, spot_price, amount_lent, timestamp);

        } catch (RemoteException exp) {
            ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);

            // attempt reconnect
            reconnectWorld(null);
        } catch (NoClassDefFoundError servError) {
            // world server may have crashed or inactive :(
            System.out.println("[Warning] World Server may be inacctive or crashed. Please restart.");
            servError.printStackTrace();
        }
    }

    private static final class ShutDownListener implements Runnable {

        @Override
        public void run() {
            System.out.println("Shutdown hook task.....");

            // Candles
            BacklogCommitTask_Tickers.BacklogTimerPersistingTask();
            BacklogCommitTask_Tickers.ImmediateBacklogTimerPersistingTask();

            // Swaps data
            BacklogCommitTask_Swaps.BacklogTimerPersistingTask();
            BacklogCommitTask_Swaps.ImmediateBacklogTimerPersistingTask();

            // Trades
            BacklogCommitTask_Trades.BacklogTimerPersistingTask();
        }
    }
}
