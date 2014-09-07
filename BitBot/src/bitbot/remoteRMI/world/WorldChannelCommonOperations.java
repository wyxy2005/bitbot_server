package bitbot.remoteRMI.world;

import java.rmi.RemoteException;

/**
 *
 * @author z
 */
public interface WorldChannelCommonOperations {

    public void broadcastMessage(byte[] message) throws RemoteException;
    
    public void broadcastPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) throws RemoteException;
    
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) throws RemoteException;
   
    public void broadcastSwapData(String ExchangeCurrency, float rate, float spot_price, double amount_lent, int timestamp) throws RemoteException;
}
