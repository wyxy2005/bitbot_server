package bitbot.remoteRMI.world;

import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author z
 */
public interface WorldChannelCommonOperations {

    public void broadcastMessage(byte[] message) throws RemoteException;
   
}
