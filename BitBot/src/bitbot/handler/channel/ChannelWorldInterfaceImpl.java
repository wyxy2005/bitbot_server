package bitbot.handler.channel;

import bitbot.remoteRMI.ChannelWorldInterface;
import bitbot.remoteRMI.encryption.XorClientSocketFactory;
import bitbot.remoteRMI.encryption.XorServerSocketFactory;
import bitbot.util.packets.ServerSocketExchangePacket;
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
    public void broadcastPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) throws RemoteException {
        server.getTickerTask().recievedNewUnmaturedData(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio, last);

        //System.out.println(String.format("[Info] Latest info from other peers %s [%d], Price: %f, Amount: %f", ExchangeCurrencyPair, date, price, amount));
        if (ChannelServer.getInstance().isEnableSocketStreaming()
                && ChannelServer.getInstance().getServerSocketExchangeHandler() != null) {
            ChannelServer.getInstance().getServerSocketExchangeHandler().broadcastMessage(ServerSocketExchangePacket.getPriceChanges(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio, last));
        }
    }

    @Override
    public void broadcastNewTradesEntry(String ExchangeCurrency, float price, double amount, long LastPurchaseTime, byte type) {
        server.getTradesTask().receivedNewTradesEntry_OtherPeers(ExchangeCurrency, price, amount, LastPurchaseTime, type);

        if (ChannelServer.getInstance().isEnableSocketStreaming()
                && ChannelServer.getInstance().getServerSocketExchangeHandler() != null) {
            ChannelServer.getInstance().getServerSocketExchangeHandler().broadcastMessage(ServerSocketExchangePacket.getNewTrades(ExchangeCurrency, price, amount, LastPurchaseTime, type));
        }
    }

    @Override
    public void broadcastNewGraphEntry(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) throws RemoteException {
        server.getTickerTask().receivedNewGraphEntry_OtherPeers(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio);
    }

    @Override
    public void broadcastSwapData(String ExchangeCurrency, float rate, float spot_price, double amount_lent, int timestamp) {
        server.getSwapsTask().receivedNewGraphEntry_OtherPeers(ExchangeCurrency, rate, spot_price, amount_lent, timestamp);
    }
}
