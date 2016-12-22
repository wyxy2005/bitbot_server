package bitbot.remoteRMI;

import bitbot.cache.tickers.TickerItemData;
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
    
    /**
     * Returns the instant spot price of the Exchange, currencypair that's indexed on the 
     * world server
     * 
     * @param  ExchangeCurrencyPair
     *         A {@code String}
     * @throws RemoteException
     * @return price 0 if unavailable.
     */
    public float getInstantSpotPrice(String ExchangeCurrencyPair) throws RemoteException;
    
    /**
     * Returns the instant spot price of the Exchange, currencypair that's indexed on the 
     * world server
     * 
     * @param  ExchangeCurrencyPair
     *         A {@code String}
     * @throws RemoteException
     * @return TickerItemData null if unavailable
     */
    public TickerItemData getInstantSpotMinuteTicker(String ExchangeCurrencyPair) throws RemoteException;
}
