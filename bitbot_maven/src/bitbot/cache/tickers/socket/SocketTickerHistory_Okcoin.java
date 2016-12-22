package bitbot.cache.tickers.socket;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.tickers.TickerHistoryInterface;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

/**
 *
 * @author zheng
 */
public class SocketTickerHistory_Okcoin extends IoHandlerAdapter implements TickerHistoryInterface {

    private NioSocketConnector connector;
    private IoSession session;

    private final boolean enableTrackTrades;
    private final String ExchangeSite;
    private final String CurrencyPair;

    private static final String SOCKET_URL = "insight.coinpit.io/socket.io/?EIO=3&transport=websocket&sid=5lCnJZ6ylhGKuTocAB6A"; // "https://live.coinpit.me"

    public SocketTickerHistory_Okcoin(boolean enableTrackTrades, String ExchangeSite, String CurrencyPair) {
        this.enableTrackTrades = enableTrackTrades;
        this.ExchangeSite = ExchangeSite;
        this.CurrencyPair = CurrencyPair;

        initializeSocketConnection();
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        if (session == null || session.isClosing() || connector == null || connector.isDisposed() || !connector.isActive()) {
            initializeSocketConnection();
        }
        return null;
    }

    private void disposeConnection() {
        // Clear previous object
        if (session != null) {
            session.close(true);
            session = null;
        }
        if (connector != null) {
            try {
                connector.dispose();
            } catch (Exception exp) {
            }
            connector = null;
        }
    }

    private void initializeSocketConnection() {
        // Initialize
        connector = new NioSocketConnector();

        // Codec
        connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
        //  connector.getFilterChain().addLast("logger", new LoggingFilter());
        connector.setHandler(this);

        connector.getSessionConfig().setReadBufferSize(512);
        connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        try {

            ConnectFuture future = connector.connect(new InetSocketAddress(SOCKET_URL, 80));
            session = future.getSession();
            future.awaitUninterruptibly();

        } catch (RuntimeIoException e) {
            System.err.println("Failed to connect.");
            e.printStackTrace();
        }

    }

    @Override
    public void sessionOpened(IoSession session) {
        final String firstMsg = String.format("request:{\"symbolId\":\"%s\",\"version\":1,\"msgType\":\"reqMarketDepthTop\",\"requestIndex\":1405131204513}",
                CurrencyPair.replace("_", "").toLowerCase());// btccny
        System.out.println(firstMsg);
        //  session.write(firstMsg);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        String msgReceived = (String) message;

        System.out.println(msgReceived);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close(true);
    }
}
