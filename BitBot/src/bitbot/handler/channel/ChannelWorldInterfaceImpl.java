package bitbot.handler.channel;

import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author z
 */
public class ChannelWorldInterfaceImpl extends UnicastRemoteObject implements ChannelWorldInterface {

    private static final long serialVersionUID = 781525689948864492L;
    private ChannelServer server;

    public ChannelWorldInterfaceImpl() throws RemoteException {
        super(0, new XorClientSocketFactory(), new XorServerSocketFactory());
    }

    public ChannelWorldInterfaceImpl(ChannelServer server) throws RemoteException {
        super(0, new XorClientSocketFactory(), new XorServerSocketFactory());
        this.server = server;
    }

    @Override
    public void setChannelId(byte id) throws RemoteException {

    }

    @Override
    public byte getChannelId() throws RemoteException {
        return 0;
    }

    @Override
    public String getIP() throws RemoteException {
        return "";
    }

    @Override
    public void broadcastMessage(byte[] message) throws RemoteException {

    }

    @Override
    public void shutdown(int exitCode) throws RemoteException {

    }

    @Override
    public void broadcastPriceChanges(TradeHistoryBuySellEnum type, String ExchangeCurrencyPair, float price, float amount, long date, int tradeid) throws RemoteException {
       //System.out.println(String.format("[Info] Latest info from other peers %s [%d], Price: %f, Amount: %f", ExchangeCurrencyPair, date, price, amount));
    }

    @Override
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur) throws RemoteException {
        server.getTickerTask().receivedNewGraphEntry_OtherPeers(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur);
    }
}
