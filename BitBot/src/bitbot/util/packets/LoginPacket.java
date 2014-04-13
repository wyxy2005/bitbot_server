package bitbot.util.packets;

import bitbot.handler.SendPacketOpcode;
import bitbot.server.Constants;
import bitbot.util.encryption.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author z
 */
public class LoginPacket {

    public static final byte[] getHello(final short version, final byte[] sendIv, final byte[] recvIv) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(version);
        mplew.writeMapleAsciiString(Constants.ServerClientMinorPatchVer);
        mplew.write(recvIv);
        mplew.write(sendIv);

        return mplew.getPacket();
    }

    public static final byte[] getPing() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

}
