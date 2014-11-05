package bitbot.handler;

import bitbot.client.Client;
import bitbot.handler.channel.ChannelServer;
import bitbot.handler.mina.PacketDecoder;
import bitbot.server.Constants;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.util.encryption.HexTool;
import bitbot.util.Randomizer;
import bitbot.util.StringPool;
import bitbot.util.encryption.AESOFB;
import bitbot.util.encryption.input.ByteArrayByteStream;
import bitbot.util.encryption.input.EndOfFileException;
import bitbot.util.encryption.input.GenericSeekableLittleEndianAccessor;
import bitbot.util.encryption.input.SeekableLittleEndianAccessor;
import bitbot.util.packets.LoginPacket;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author z
 */
public class ServerClientHandler extends IoHandlerAdapter {

    private static final Map<Short, RecvPacketOpcode> Header = new HashMap();

    public ServerClientHandler() {
        populatedRecvCode();
    }

    public final void populatedRecvCode() {
        Header.clear();
        for (final RecvPacketOpcode recv : RecvPacketOpcode.values()) {
            Header.put((short) recv.getValue(), recv);
        }
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
        final byte ivRecv[] = new byte[]{(byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255)};
        final byte ivSend[] = new byte[]{(byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255), (byte) Randomizer.nextInt(255)};

        final Client client = new Client(
                new AESOFB(ivSend, (short) (0xFFFF - Constants.ServerClientVersion)), // Sent Cypher
                new AESOFB(ivRecv, Constants.ServerClientVersion), // Recv Cypher
                session);

        session.setAttribute(PacketDecoder.DECODER_STATE_KEY, new PacketDecoder.DecoderState());

        session.write(LoginPacket.getHello(Constants.ServerClientVersion, ivSend, ivRecv));

        session.setAttribute(StringPool.CLIENT_KEY, client);
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 30);
        session.getConfig().setIdleTime(IdleStatus.WRITER_IDLE, 30);

        System.out.println("IoSession opened " + session.getRemoteAddress().toString());
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
        super.sessionClosed(session);
    }

    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {
        final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream((byte[]) message));
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
        }
    }

    @Override
    public void sessionIdle(final IoSession session, final IdleStatus status) throws Exception {
        final Client client = (Client) session.getAttribute(StringPool.CLIENT_KEY);

        /*
         * if (client != null && client.getPlayer() != null) {
         * System.out.println("Player "+ client.getPlayer().getName() +" went
         * idle"); }
         */
        if (client != null) {
            client.sendPing_MinaSocketCore();
        }
        super.sessionIdle(session, status);
    }

    public static final void handlePacket(final RecvPacketOpcode header, final SeekableLittleEndianAccessor slea, final Client c) {
        switch (header) {
            default:
                System.out.println(String.format("[UNHANDLED] Recv [%s] found", header.toString()));
                break;
        }
    }
}
