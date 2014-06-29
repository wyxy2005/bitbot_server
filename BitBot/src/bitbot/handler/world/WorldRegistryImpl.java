package bitbot.handler.world;

import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import bitbot.remoteRMI.world.WorldRegistry;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.util.encryption.SHA256;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author twili_000
 */
public class WorldRegistryImpl extends UnicastRemoteObject implements WorldRegistry {

    private static final long serialVersionUID = -170574938159280746L;
    private static WorldRegistryImpl instance = null;

    // List of channels connected to this world
    private static final Map<Byte, ChannelWorldInterface> channelServer = new LinkedHashMap();
    private static final Map<Byte, WorldChannelInterface> channelServer_wci = new LinkedHashMap();
    
    // Timestamp
    private static final Calendar cal = Calendar.getInstance();

    // hashes for authentication
    private static final String[] ChannelServerHashes = {
        "4e79a7e08f6de175f7b2f89fb38b9cc140b7711f927543751b79c05dfc14cf3e", // channel 1 [For serving of client data] asdas15as1d51as5d4yt89j4657u56o7056034;>:#R>:#@$)@O$23dfg541df
        "38c325602b59ee29cef7ee79edee9a0bae0a1edc6278fd2f5da0b13df82cf603", // channel 2 [For caching of data from exchange] asd4d5fg154348544y51gh5641uy89k451h2d3f1h56451y8t48t345r"#:":#@"$R:@™
    };

    private WorldRegistryImpl() throws RemoteException {
        super(0, new XorClientSocketFactory(), new XorServerSocketFactory());
    }

    public static WorldRegistryImpl getInstance() {
        if (instance == null) {
            try {
                System.out.println("[Info] Initializing WorldRegistryImpl...");
                instance = new WorldRegistryImpl();
            } catch (RemoteException e) {
                // can't do much anyway we are fucked ^^
                throw new RuntimeException(e);
            }
        }
        return instance;
    }
    
    public ChannelWorldInterface getChannel(byte channel) {
        return channelServer.get(channel);
    }
    
    public Calendar getCalendar() {
        return cal;
    }
    
    public Set<Byte> getChannelServer() {
	return new HashSet(channelServer.keySet());
    }

    @Override
    public WorldChannelInterface registerChannelServer(String authKey, final ChannelWorldInterface cb, boolean isReconnect) throws RemoteException {
        authKey = SHA256.sha256(authKey);

        byte channelId = -1;
        for (int i = 0; i < ChannelServerHashes.length; i++) {
            if (ChannelServerHashes[i].equals(authKey)) {
                channelId = (byte) i;
            }
        }

        if (channelId > -1) {
            if (channelServer.containsKey(channelId) && !isReconnect) {
                ChannelWorldInterface oldch = channelServer.get(channelId);
                try {
                    System.out.println(String.format("[Info] Unregistering existing channel %d.", channelId));
                    oldch.shutdown(0);
                } catch (RemoteException ce) {
                    // silently ignore as we assume that the server is offline
                }
            }
            channelServer.put(channelId, cb);
            cb.setChannelId(channelId);
            WorldChannelInterface ret = new WorldChannelInterfaceImpl(cb, channelId);

            channelServer_wci.put(channelId, ret); // Keep reference.
            
            System.out.println(String.format("[Info] Channel %d is online.", channelId));
            return ret;
        }
        throw new RuntimeException("Couldn't find a channel with the given key (" + authKey + ")");
    }

    @Override
    public void deregisterChannelServer(final byte channel) throws RemoteException {
        deregisterChannelServer(channel, null);
    }

    public void deregisterChannelServer(final byte channel, final Exception e) throws RemoteException {
        /*channelServer.remove(channel);
         for (final LoginWorldInterface wli : loginServer) {
         wli.channelOffline(channel);
         }*/
         if (e != null) {
             ServerLog.RegisterForLoggingException(ServerLogType.ShutdownError, e);
         }
        System.out.println("[Info] Channel " + channel + " is disconnected/offline.");
    }

    @Override
    public String getStatus() throws RemoteException {
        return "";
    }
}
