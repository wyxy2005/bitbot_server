package bitbot.handler;

import bitbot.client.Client;
import bitbot.handler.channel.ChannelServer;
import bitbot.handler.mina.BlackListFilter;
import bitbot.handler.mina.PacketDecoder;
import bitbot.Constants;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.StringPool;
import bitbot.util.encryption.input.EndOfFileException;
import bitbot.util.packets.ServerSocketExchangePacket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class ServerSocketExchangeHandler extends IoHandlerAdapter {

    // Socket
    private SocketAcceptor acceptor;
    public InetSocketAddress InetSocketadd;

    private ServerSocketExchangeHandler() {
    }

    public static ServerSocketExchangeHandler connect(String ipAddr) throws Exception {
        ServerSocketExchangeHandler instance = new ServerSocketExchangeHandler();

        IoBuffer.setUseDirectBuffer(false);
        IoBuffer.setAllocator(new SimpleBufferAllocator());

        instance.acceptor = new NioSocketAcceptor(Runtime.getRuntime().availableProcessors() * 2);
        instance.acceptor.setReuseAddress(true);
        instance.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 50);
        instance.acceptor.getSessionConfig().setTcpNoDelay(true);

        // filters
        instance.acceptor.getFilterChain().addFirst("blacklist", new BlackListFilter());

        TextLineCodecFactory tlcFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        tlcFactory.setDecoderMaxLineLength(1024);
        tlcFactory.setEncoderMaxLineLength(1024);
        instance.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(tlcFactory));

        instance.InetSocketadd = new InetSocketAddress(InetAddress.getByName(ipAddr), Constants.SocketPort_Stream);
        instance.acceptor.setHandler(instance);
        instance.acceptor.bind(instance.InetSocketadd);

        System.out.println(String.format("[Info] Listening on socket IP %s:%d", ipAddr, Constants.SocketPort_Stream));

        return instance;
    }

    public void Disconnect() {
        acceptor.unbind();
        acceptor.dispose();
    }

    @Override
    public void messageSent(final IoSession session, final Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(final IoSession session, final Throwable cause) throws Exception {
        if (cause instanceof EndOfFileException) {
            session.close(true);
//	    cause.printStackTrace();
        } else if (!(cause instanceof IOException)) { // Happens when disconnecting via force exit, so don't print!
            cause.printStackTrace();
        }
    }

    @Override
    public void sessionOpened(final IoSession session) throws Exception {
        if (ChannelServer.getInstance().isShutdown()) {
            session.close(true);
            return;
        }
        final Client client = new Client(session);

        session.setAttribute(PacketDecoder.DECODER_STATE_KEY, new PacketDecoder.DecoderState());

        session.setAttribute(StringPool.CLIENT_KEY, client);
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 30);
        session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, 30);

        System.out.println("[Socket] Session accepted " + session.getRemoteAddress().toString());

        // Say hello
        session.write(ServerSocketExchangePacket.getHello());
    }

    @Override
    public void sessionClosed(final IoSession session) throws Exception {
        synchronized (session) {
            final Client client = (Client) session.getAttribute(StringPool.CLIENT_KEY);
            try {
                if (client != null) {
                    client.closeSession();
                }
            } finally {
                session.removeAttribute(StringPool.CLIENT_KEY);
                session.close(true);
            }
        }
        System.out.println("[Socket] Session closed " + session.getRemoteAddress().toString());
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        final String receiveMessage = (String) message;
        final Client c = (Client) session.getAttribute(StringPool.CLIENT_KEY);

        final long cTime = System.currentTimeMillis();
        final long Differences = cTime - c.LastCapturedTimeMillis_500MSThreshold;

        if (Differences < 500) { // within 500ms
            if (!c.FlagPendingDisconnection) {
                c.PacketSpamCountWithinHalfSecond++;

                // 70 should be the acceptable level, but we will test with 200 first to make sure it doesn't affect laggers.
                if (c.PacketSpamCountWithinHalfSecond > 10) { // Spam > 70 packet within 500ms = dc.
                    c.closeSession();

                    InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
                    InetAddress inetAddress = socketAddress.getAddress();

                    ServerLog.RegisterForLogging(ServerLogType.PacketSpam,
                            String.format("IP : %s, Count : %d\r\nData : %s",
                                    inetAddress.getHostAddress(),
                                    c.PacketSpamCountWithinHalfSecond,
                                    receiveMessage));
                    return;
                }
            }
        } else {
            c.LastCapturedTimeMillis_500MSThreshold = cTime;
            c.PacketSpamCountWithinHalfSecond = 0;
        }
        // This is also used throughout the packet handler to determine the current time instead of calling System.currentTimeMillis() again
        c.LastCapturedTimeMillis = cTime;
        //	    System.out.println("Differences : "+(Differences)+", count : "+c.PacketSpamCountWithinHalfSecond+", cTime : " + cTime);

        // Handle packet
        try {
            JSONParser parser = new JSONParser(); // Init parser

            ContainerFactory containerFactory = new ContainerFactory() {
                @Override
                public List creatArrayContainer() {
                    return new LinkedList();
                }

                @Override
                public Map createObjectContainer() {
                    return new LinkedHashMap();
                }
            };
            LinkedHashMap jsonArray = (LinkedHashMap) parser.parse(receiveMessage, containerFactory);

            String type = jsonArray.get("type").toString();
            switch (type) {
                case "ping": {
                    c.pongReceived();
                    break;
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        final Client client = (Client) session.getAttribute(StringPool.CLIENT_KEY);

        if (client != null) {
            client.sendPing_MinaSocketCore();
        }
        super.sessionIdle(session, status);
    }

    public void broadcastMessage(String msg) {
        if (acceptor.isActive()) {
            acceptor.broadcast(msg);
        }
    }
}
