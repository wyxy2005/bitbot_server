package bitbot.handler.channel;

import bitbot.handler.ServerExchangeHandler;
import bitbot.server.Constants;
import bitbot.cache.news.NewsCacheTask;
import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.history.BacklogCommitTask;
import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import bitbot.handler.ServerClientHandler;
import bitbot.handler.mina.BlackListFilter;
import bitbot.handler.mina.MapleCodecFactory;
import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.world.WorldRegistry;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.util.mssql.DatabaseConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

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
    private boolean 
            isShutdown = false, 
            finishedShutdown = false,
            isReconnectState = false;
    
    private ServerExchangeHandler serverExchangeHandler = null;

    private ServerClientHandler serverClientHandler = null;
    private SocketAcceptor acceptor;
    private InetSocketAddress InetSocketadd;
   

    private TickerCacheTask tickerTask = null;
    private NewsCacheTask newsTask = null;

    // Properties
    private static Properties props = null;
    private static boolean 
            Props_EnforceCloudFlareNetwork = false,
            Props_EnableTickerHistoryDatabaseCommit = false,
            Props_EnableTickerHistory = false,
            Props_EnableSQLDataAcquisition = false,
            Props_EnableSocketStreaming = false,
            Props_EnableDebugSessionPrints = false;
    private static String 
            Props_SocketIPAddress = "127.0.0.1",
            Props_WorldIPAddress = "127.0.0.1";
    private static short
            Props_SocketPort = 8082,
            Props_WorldRMIPort = 5454;

    // Etc
    private final List<String> CachingCurrencyPair = new ArrayList();

    private ChannelServer() {

    }
    
    public static final WorldRegistry getWorldRegistry() {
	return worldRegistry;
    }


    public static ChannelServer getInstance() {
        if (ch.serverExchangeHandler == null) {
            ch.initializeChannelServer();
        }
        return ch;
    }

    public void initializeChannelServer() {
        if (serverExchangeHandler == null) {
            try {
                // Init properties
                System.out.println("[Info] Loading server properties..");
                
                if (props == null) {
                    props = new Properties();
                    try (FileReader is = new FileReader("server.properties")) {
                        props.load(is);
                    }
                }
                Props_EnforceCloudFlareNetwork = Boolean.parseBoolean(props.getProperty("server.EnforceCloudFlareNetwork"));
                Props_EnableTickerHistory = Boolean.parseBoolean(props.getProperty("server.EnableTickerHistory"));
                Props_EnableTickerHistoryDatabaseCommit = Boolean.parseBoolean(props.getProperty("server.EnableTickerHistoryDatabaseCommit"));
                Props_EnableSQLDataAcquisition = Boolean.parseBoolean(props.getProperty("server.EnableSQLDataAcquisition"));
                Props_SocketIPAddress = props.getProperty("server.SocketIPAddress");
                Props_SocketPort = Short.parseShort(props.getProperty("server.SocketPort"));
                Props_EnableSocketStreaming = Boolean.parseBoolean(props.getProperty("server.EnableSocketStreaming"));
                Props_EnableDebugSessionPrints = Boolean.parseBoolean(props.getProperty("server.EnableDebugSessionPrints"));
                Props_WorldIPAddress = props.getProperty("server.WorldIPAddress");
                Props_WorldRMIPort = Short.parseShort(props.getProperty("server.WorldRMIPort"));
                
                
                // Establish RMI connection
                System.out.println(String.format("[Info] Locating world server RMI connection at %s:%d..", Props_WorldIPAddress, Props_WorldRMIPort));
                
                final Registry registry = LocateRegistry.getRegistry(Props_WorldIPAddress, Props_WorldRMIPort, new SslRMIClientSocketFactory());
                worldRegistry = (WorldRegistry) registry.lookup(Constants.Server_AzureAuthorization);

                cwi = new ChannelWorldInterfaceImpl(this);
                wci = worldRegistry.registerChannelServer("Test123", cwi, false);

                // End
                System.out.println("[Info] Loading tasks..");
                
                serverExchangeHandler = ServerExchangeHandler.Connect();

                LoadCurrencyPairTables(false);

                tickerTask = new TickerCacheTask(); // init automatically
                newsTask = new NewsCacheTask();
                
                if (isEnableSocketStreaming()) {
                    InitializeClientServer();   
                }
                
                // Shutdown hooks
                System.out.println("[Info] Registering shutdown hooks");
                Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
            } catch (Exception exp) {
                System.out.println("[Warning] Failed to start Channel server, please ensure the port is unused.");
                exp.printStackTrace();

                if (serverExchangeHandler != null) {
                    serverExchangeHandler.Disconnect();
                }
                serverExchangeHandler = null;
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
			wci2 = reg.registerChannelServer("Test123", cwi2, true);

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
    
    public void InitializeClientServer() {
        if (acceptor == null) {
            IoBuffer.setUseDirectBuffer(false);
            IoBuffer.setAllocator(new SimpleBufferAllocator());

            acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() * 4);
            acceptor.setReuseAddress(true);
            acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
            acceptor.getSessionConfig().setTcpNoDelay(true);
            acceptor.getFilterChain().addFirst("blacklist", new BlackListFilter());
            acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MapleCodecFactory())); // decrypt last

            try {
                serverClientHandler = new ServerClientHandler();
                InetSocketadd = new InetSocketAddress(InetAddress.getByName(Props_SocketIPAddress), Props_SocketPort);
                acceptor.setHandler(serverClientHandler);
                acceptor.bind(InetSocketadd);

                System.out.println(String.format("[Socket] Listening on %s:%d", Props_SocketIPAddress, Props_SocketPort));
            } catch (IOException e) {
                System.out.println(String.format("[Socket] Failed to listen on %s%d", Props_SocketIPAddress, Props_SocketPort));
            }
        }
    }

    public void LoadCurrencyPairTables(boolean reload) {
        if (!CachingCurrencyPair.isEmpty() && !reload) {
            return;
        }

        File f = new File(Constants.CurrencyPairFile);
        if (!f.exists()) {
            try {
                f.createNewFile();
                try (FileWriter Writer = new FileWriter(Constants.CurrencyPairFile, false)) {
                    Writer.append(Constants.DefaultCurrencyPair);
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString() + " Error accessing file system to create Blocked IP list.");
            }
        }
        try (FileReader Reader = new FileReader(Constants.CurrencyPairFile)) {
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
            System.out.println(e.toString() + " Error accessing Blocked IP list.");
        }
    }

    public List<String> getCachingCurrencyPair() {
        return CachingCurrencyPair;
    }

    public TickerCacheTask getTickerTask() {
        return tickerTask;
    }

    public NewsCacheTask getNewsTask() {
        return newsTask;
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
    
    public boolean isEnableSocketStreaming() {
        return Props_EnableSocketStreaming;
    }

    public boolean isEnableDebugSessionPrints() {
        return Props_EnableDebugSessionPrints;
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

    public void BroadcastConnectedClients(TradeHistoryBuySellEnum type, String ExchangeCurrencyPair, int tradeid) {
        // TODO
    }

    private final class ShutDownListener implements Runnable {

        @Override
        public void run() {
            System.out.println("Shutdown hook task.....");
            
            BacklogCommitTask.BacklogTimerPersistingTask();
            BacklogCommitTask.ImmediateBacklogTimerPersistingTask();
        }
    }
}
