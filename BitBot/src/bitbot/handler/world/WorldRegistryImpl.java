package bitbot.handler.world;

import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.world.WorldRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author twili_000
 */
public class WorldRegistryImpl extends UnicastRemoteObject implements WorldRegistry {

    private static final long serialVersionUID = -170574938159280746L;
    private static WorldRegistryImpl instance = null;
    
    // Others
    private static final Map<Byte, ChannelWorldInterface> channelServer = new LinkedHashMap();
    private static final Map<Byte, WorldChannelInterface> channelServer_wci = new LinkedHashMap();

    
    private WorldRegistryImpl() throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
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

    @Override
    public WorldChannelInterface registerChannelServer(final String authKey, final ChannelWorldInterface cb, boolean isReconnect) throws RemoteException {
	/*Connection con = DatabaseConnection.getConnection();
	try (PreparedStatement ps = con.prepareStatement("SELECT * FROM auth_server_channel WHERE `key` = SHA1(?) AND world = ?")) {
	    ps.setString(1, authKey);
	    ps.setInt(2, WorldServer.getInstance().getWorldId());
	    try (ResultSet rs = ps.executeQuery()) {
		if (rs.next()) {
		    byte channelId = rs.getByte("number");
		    if (channelId < 1) {
			channelId = getFreeChannelId();
			if (channelId == -1) {
			    throw new RuntimeException("Maximum channels reached");
			}
		    } else {
			if (channelServer.containsKey(channelId) && !isReconnect) {
			    ChannelWorldInterface oldch = channelServer.get(channelId);
			    try {
				oldch.shutdown(0);
			    } catch (ConnectException ce) {
				// silently ignore as we assume that the server is offline
			    }
			    // int switchChannel = getFreeChannelId();
			    // if (switchChannel == -1) {
			    // throw new RuntimeException("Maximum channels reached");
			    // }
			    // ChannelWorldInterface switchIf = channelServer.get(channelId);
			    // deregisterChannelServer(switchChannel);
			    // channelServer.put(switchChannel, switchIf);
			    // switchIf.setChannelId(switchChannel);
			    // for (LoginWorldInterface wli : loginServer) {
			    // wli.channelOnline(switchChannel, switchIf.getIP());
			    // }
			}
		    }
		    channelServer.put(channelId, cb);
		    cb.setChannelId(channelId);
		    WorldChannelInterface ret = new WorldChannelInterfaceImpl(cb, rs.getInt("channelid"));

		    channelServer_wci.put(channelId, ret); // Keep reference.
		    return ret;
		}
	    }
	} catch (SQLException ex) {
	    ex.printStackTrace();
	    System.err.println("Encountered database error while authenticating channelserver" + ex);
	}*/
	throw new RuntimeException("Couldn't find a channel with the given key (" + authKey + ")");
    }

    public void deregisterChannelServer(final byte channel) throws RemoteException {
	deregisterChannelServer(channel, null);
    }

    public void deregisterChannelServer(final byte channel, final Exception e) throws RemoteException {
	/*channelServer.remove(channel);
	for (final LoginWorldInterface wli : loginServer) {
	    wli.channelOffline(channel);
	}
	if (e != null) {
	    ServerLog.RegisterForLoggingException(ServerLogType.ShutdownError, e);
	}*/
	System.out.println("Channel " + channel + " is disconnected/offline.");
    }

    public String getStatus() throws RemoteException {
        return "";
    }
}
