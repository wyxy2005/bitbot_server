package bitbot.remoteRMI;

import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import bitbot.remoteRMI.world.WorldChannelCommonOperations;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author z
 */
public interface WorldChannelInterface extends Remote, WorldChannelCommonOperations {

    public long getServerStartTime() throws RemoteException;
    
    public void serverReady() throws RemoteException;

    public String getIP(byte channel) throws RemoteException;

    public boolean isAvailable() throws RemoteException;

    public ChannelWorldInterface getChannelInterface(byte channel) throws RemoteException;
}
