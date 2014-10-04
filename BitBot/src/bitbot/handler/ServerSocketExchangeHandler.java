package bitbot.handler;

import bitbot.client.Client;
import bitbot.handler.channel.ChannelServer;
import bitbot.handler.mina.BlackListFilter;
import bitbot.handler.mina.PacketDecoder;
import bitbot.server.Constants;
import bitbot.util.StringPool;
import bitbot.util.encryption.input.EndOfFileException;
import bitbot.util.packets.ServerSocketExchangePacket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.buffer.SimpleBufferAllocator;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

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
        instance.acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
        instance.acceptor.getSessionConfig().setTcpNoDelay(true);

        // filters
        instance.acceptor.getFilterChain().addFirst("blacklist", new BlackListFilter());
        instance.acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

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
                    client.disconnect();
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
        /*  final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
         final RecvPacketOpcode code = Header.get(slea.readShort());

         if (code != null) {
         final Client c = (Client) session.getAttribute(StringPool.CLIENT_KEY);

         final long cTime = System.currentTimeMillis();
         final long Differences = cTime - c.LastCapturedTimeMillis_500MSThreshold;
         if (Differences < 500) { // within 500ms
         if (!c.FlagPendingDisconnection) {
         c.PacketSpamCountWithinHalfSecond++;

         // 70 should be the acceptable level, but we will test with 200 first to make sure it doesn't affect laggers.
         if (c.PacketSpamCountWithinHalfSecond > 200) { // Spam > 70 packet within 500ms = dc.
         c.flagPendingDisconnection();

         InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
         InetAddress inetAddress = socketAddress.getAddress();

         ServerLog.RegisterForLogging(ServerLogType.PacketSpam,
         String.format("[%s 0x%s] IP : %s, Count : %d\nData : %s\nString : %s",
         code.name(),
         Integer.toHexString(code.getValue()),
         inetAddress.getHostAddress(),
         c.PacketSpamCountWithinHalfSecond,
         HexTool.toString((byte[]) message),
         HexTool.toStringFromAscii((byte[]) message)));
         }
         }
         } else {
         c.LastCapturedTimeMillis_500MSThreshold = cTime;
         c.PacketSpamCountWithinHalfSecond = 0;
         }
         // This is also used throughout the packet handler to determine the current time instead of calling System.currentTimeMillis() again
         c.LastCapturedTimeMillis = cTime;
         //	    System.out.println("Differences : "+(Differences)+", count : "+c.PacketSpamCountWithinHalfSecond+", cTime : " + cTime);

         handlePacket(code, slea, c);
         } else if (Constants.enableUnhandledPacketLogging) { // Console output part for debugging
         System.out.println(String.format("[Unhandled Packet] %s\nString : %s", HexTool.toString((byte[]) message), HexTool.toStringFromAscii((byte[]) message)));
         }*/
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
