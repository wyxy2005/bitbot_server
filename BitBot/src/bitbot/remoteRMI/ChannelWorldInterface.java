package bitbot.remoteRMI;

import bitbot.remoteRMI.world.WorldChannelCommonOperations;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author z
 */
public interface ChannelWorldInterface extends Remote, WorldChannelCommonOperations {
    
    public void setChannelId(byte id) throws RemoteException;

    public byte getChannelId() throws RemoteException;

    public String getIP() throws RemoteException;

}
