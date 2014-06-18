package bitbot.remoteRMI;

import bitbot.remoteRMI.world.WorldChannelCommonOperations;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 *
 * @author z
 */
public interface WorldChannelInterface extends Remote, WorldChannelCommonOperations {

    public long getServerStartTime() throws RemoteException;

    public void ToLogin_ReloadIPBans() throws RemoteException;

    public void serverReady() throws RemoteException;

    public String getIP(byte channel) throws RemoteException;

    public boolean isAvailable() throws RemoteException;

    public ChannelWorldInterface getChannelInterface(byte channel) throws RemoteException;

}
