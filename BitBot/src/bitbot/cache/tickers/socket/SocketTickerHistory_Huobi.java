package bitbot.cache.tickers.socket;

import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.tickers.TickerHistoryInterface;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 *
 * @author zheng
 */
public class SocketTickerHistory_Huobi extends IoHandlerAdapter implements TickerHistoryInterface {

    private NioSocketConnector connector;
    private IoSession session;

    private final boolean enableTrackTrades;
    private final String ExchangeSite;
    private final String CurrencyPair;

    public SocketTickerHistory_Huobi(boolean enableTrackTrades, String ExchangeSite, String CurrencyPair) {
        this.enableTrackTrades = enableTrackTrades;
        this.ExchangeSite = ExchangeSite;
        this.CurrencyPair = CurrencyPair;

        InitializeSocketConnection();
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {

        return null;
    }

    private void InitializeSocketConnection() {
        if (connector != null) {
            try {
                connector.dispose();
            } catch (Exception exp) {
            }
            connector = null;
        }
        connector = new NioSocketConnector();

        // Codec
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setHandler(this);

        try {
            ConnectFuture future = connector.connect(new InetSocketAddress("hq.huobi.com", 80));
            future.awaitUninterruptibly();
            session = future.getSession();

        } catch (RuntimeIoException e) {
            System.err.println("Failed to connect.");
            e.printStackTrace();
        }

    }

    @Override
    public void sessionOpened(IoSession session) {
        final String firstMsg
                = String.format("{\"symbolList\":{\"tradeDetail\":[{\"symbolId\":\"btccny\",\"pushType\":\"pushLong\"}]},\"version\":1,\"msgType\":\"reqMsgSubscribe\",\"requestIndex\":1404103038520}",
                        CurrencyPair.replace("_", "").toLowerCase());// btccny

        session.write(firstMsg);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        String msgReceived = (String) message;

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {

        session.close(true);
    }
}
