package bitbot.remoteRMI;

import bitbot.remoteRMI.world.WorldChannelCommonOperations;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author z
 */
public interface ChannelWorldInterface extends Remote, WorldChannelCommonOperations {
    
    public void setChannelId(byte id) throws RemoteException;

    public byte getChannelId() throws RemoteException;

    public String getIP() throws RemoteException;
    
    public void shutdown(int exitCode) throws RemoteException;
}
