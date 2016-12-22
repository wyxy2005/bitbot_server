package bitbot.remoteRMI.world;

import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 *
 * @author twili_000
 */
public interface WorldRegistry extends Remote {

    public WorldChannelInterface registerChannelServer(String authKey, ChannelWorldInterface cb, boolean isReconnect) throws RemoteException;

    public void deregisterChannelServer(byte channel) throws RemoteException;

    public String getStatus() throws RemoteException;
}
