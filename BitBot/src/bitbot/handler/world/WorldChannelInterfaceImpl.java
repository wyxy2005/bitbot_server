package bitbot.handler.world;

import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author z
 */
public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface {

    private static final long serialVersionUID = -5568606556235590482L;
    private ChannelWorldInterface cb;
    private int dbId;
    private boolean ready = false;

    public WorldChannelInterfaceImpl() throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public WorldChannelInterfaceImpl(ChannelWorldInterface cb, int dbId) throws RemoteException {
        super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        this.cb = cb;
        this.dbId = dbId;
    }

    @Override
    public ChannelWorldInterface getChannelInterface(byte channel) {
        return WorldRegistryImpl.getInstance().getChannel(channel);
    }

    @Override
    public boolean isAvailable() throws RemoteException {
        return true;
    }

    @Override
    public String getIP(byte channel) throws RemoteException {
        final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(channel);
        if (cwi == null) {
            return "0.0.0.0:0";
        } else {
            try {
                return cwi.getIP();
            } catch (RemoteException e) {
                WorldRegistryImpl.getInstance().deregisterChannelServer(channel, e);
                return "0.0.0.0:0";
            }
        }
    }

    @Override
    public void serverReady() throws RemoteException {
        ready = true;

        System.out.println("[Info] Channel " + cb.getChannelId() + " is online.");
    }

    public boolean isReady() {
        return ready;
    }
    
    @Override
    public long getServerStartTime() throws RemoteException {
	return WorldRegistryImpl.getInstance().getCalendar().getTimeInMillis();
    }

    @Override
    public void broadcastMessage(byte[] message) throws RemoteException {
	for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
	    final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
	    try {
		cwi.broadcastMessage(message);
	    } catch (RemoteException e) {
		WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
	    }
	}
    }

}
