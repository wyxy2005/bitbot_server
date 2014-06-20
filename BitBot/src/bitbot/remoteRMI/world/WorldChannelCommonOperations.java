package bitbot.remoteRMI.world;

import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author z
 */
public interface WorldChannelCommonOperations {

    public void broadcastMessage(byte[] message) throws RemoteException;
    
    public void broadcastPriceChanges(TradeHistoryBuySellEnum type, String ExchangeCurrencyPair, float price, float amount, long date, int tradeid) throws RemoteException;
    
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur) throws RemoteException;
   
}
