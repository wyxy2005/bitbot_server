package bitbot.handler.world;

import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import bitbot.Constants;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import java.io.FileReader;
import java.rmi.AlreadyBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

/**
 *
 * @author z
 */
public class WorldServer {

    private final static WorldServer instance = new WorldServer();

    private static Properties props = null;
    private static String Props_WorldIPAddress = "127.0.0.1";
    private static short Props_WorldRMIPort = 5454;

    public static final WorldServer getInstance() {
        return instance;
    }

    public static void initializeWorldServer(String serverPropertyFilePath) {
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
            Props_WorldIPAddress = props.getProperty("server.WorldIPAddress");
            Props_WorldRMIPort = Short.parseShort(props.getProperty("server.WorldRMIPort"));

            // RMI connection
            System.out.println(String.format("[Info] Establishing RMI connection at %s:%d", Props_WorldIPAddress, Props_WorldRMIPort));
            
            final XorClientSocketFactory srcsf = new XorClientSocketFactory();
            final XorServerSocketFactory srssf = new XorServerSocketFactory();

            final Registry registry = LocateRegistry.createRegistry(Props_WorldRMIPort/*, srcsf, srssf*/);
            registry.bind(Constants.Server_AzureAuthorization, WorldRegistryImpl.getInstance());

            // Start timer tasks
            TimerManager.start();
            MultiThreadExecutor.start();
            Schedule_DailyPriceUpdates.start();
            Schedule_PriceIndex.start();
            
            // Shutdown hooks
            System.out.println("[Info] Registering shutdown hooks");
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutDownListener()));
           
        } catch (final RemoteException ex) {
            System.err.println("Could not initialize RMI system" + ex);
            System.exit(464131385);
        } catch (final AlreadyBoundException abe) {
            System.err.println("RMI WorldRegistry already init.");
            System.exit(464131385);
        } catch (Exception exp) {
            exp.printStackTrace();
            System.exit(0);
        }
        System.out.println("[Info] World server loaded!");
    }

    private static final class ShutDownListener implements Runnable {

        @Override
        public void run() {
            System.out.println("Shutting down world server... ");

        }
    }
}
