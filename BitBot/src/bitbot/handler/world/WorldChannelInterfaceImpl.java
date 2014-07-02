package bitbot.handler.world;

import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.WorldChannelInterface;
import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author z
 */
public class WorldChannelInterfaceImpl extends UnicastRemoteObject implements WorldChannelInterface {

    private static final long serialVersionUID = -5568606556235590482L;
    private final ChannelWorldInterface cb;
    private final int dbId;
    private boolean ready = false;

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
    public void broadcastPriceChanges(TradeHistoryBuySellEnum type, String ExchangeCurrencyPair, float price, float amount, long date, int tradeid) throws RemoteException {
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastPriceChanges(type, ExchangeCurrencyPair, price, amount, date, tradeid);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }
    }
    
    @Override
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur) throws RemoteException {
        for (byte i : WorldRegistryImpl.getInstance().getChannelServer()) {
            
            if (i != dbId) { // Don't broadcast back to self
                final ChannelWorldInterface cwi = WorldRegistryImpl.getInstance().getChannel(i);
                try {
                    cwi.broadcastNewGraphEntry(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur);
                } catch (RemoteException e) {
                    WorldRegistryImpl.getInstance().deregisterChannelServer(i, e);
                }
            }
        }
    }
}
