package bitbot.handler.world;

import bitbot.cache.tickers.TickerItemData;
import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface {

    private static final long serialVersionUID = -5568606556235590482L;
    private final ChannelWorldInterface cb;
    private final int dbId;
    private boolean ready = false;

    // Summary index of the latest price propagated throughout the entire server network.
    private final Map<String, TickerItemData> index_priceMinuteTicker = new HashMap<>(); // Key = ExchangeCurrencyPair
    private final Lock index_priceMinuteTicker_Mutex = new ReentrantLock();

    private final Map<String, Float> index_priceInstantTicker = new HashMap<>(); // Key = ExchangeCurrencyPair
    private final Lock index_priceInstantTicker_Mutex = new ReentrantLock();

    public WorldChannelInterfaceImpl(ChannelWorldInterface cb, byte dbId) throws RemoteException {
        super(0, new XorClientSocketFactory(), new XorServerSocketFactory());
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

    @Override
    public void broadcastPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) throws RemoteException {
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastPriceChanges(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio, last);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }
        //System.out.println("Indexed spot price of "+ExchangeCurrencyPair+" at " + last);
        // Update index
        index_priceInstantTicker_Mutex.lock();
        try {
            if (index_priceInstantTicker.containsKey(ExchangeCurrencyPair)) {
                index_priceInstantTicker.replace(ExchangeCurrencyPair, last);
            } else {
                index_priceInstantTicker.put(ExchangeCurrencyPair, last);
            }
        } finally {
            index_priceInstantTicker_Mutex.unlock();
        }
    }
    
    /**
     * Returns the instant spot price of the Exchange, currencypair that's indexed on the 
     * world server
     * 
     * @param  ExchangeCurrencyPair
     *         A {@code String}
     * @throws RemoteException
     * @return price 0 if unavailable.
     */
    @Override
    public float getInstantSpotPrice(String ExchangeCurrencyPair) throws RemoteException {
        //System.out.println("getInstantSpotPrice = " + ExchangeCurrencyPair);
        if (index_priceInstantTicker.containsKey(ExchangeCurrencyPair)) {
            return index_priceInstantTicker.get(ExchangeCurrencyPair);
        }
        return 0;
    }

    @Override
    public void broadcastNewTradesEntry(String ExchangeCurrency, float price, double amount, long LastPurchaseTime, byte type) throws RemoteException {
        // Broadcast to clients
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastNewTradesEntry(ExchangeCurrency, price, amount, LastPurchaseTime, type);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }
    }
    
    
    @Override
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) throws RemoteException {
        // Broadcast to clients
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastNewGraphEntry(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }

        // Update index
        final TickerItemData data = new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio);
        
        index_priceMinuteTicker_Mutex.lock();
        try {
            if (index_priceMinuteTicker.containsKey(ExchangeCurrencyPair)) {
                index_priceMinuteTicker.replace(ExchangeCurrencyPair, data);
            } else {
                index_priceMinuteTicker.put(ExchangeCurrencyPair, data);
            }
        } finally {
            index_priceMinuteTicker_Mutex.unlock();
        }
    }

    /**
     * Returns the instant spot price of the Exchange, currencypair that's indexed on the 
     * world server
     * 
     * @param  ExchangeCurrencyPair
     *         A {@code String}
     * @throws RemoteException
     * @return TickerItemData null if unavailable
     */
    @Override
    public TickerItemData getInstantSpotMinuteTicker(String ExchangeCurrencyPair) throws RemoteException {
        if (index_priceMinuteTicker.containsKey(ExchangeCurrencyPair)) {
            return index_priceMinuteTicker.get(ExchangeCurrencyPair);
        }
        return null;
    }
    
    @Override
    public void broadcastSwapData(String ExchangeCurrency, float rate, float spot_price, double amount_lent, int timestamp) throws RemoteException {
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {

            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastSwapData(ExchangeCurrency, rate, spot_price, amount_lent, timestamp);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }
    }
}
