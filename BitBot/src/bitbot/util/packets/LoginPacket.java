package bitbot.util.packets;

import bitbot.handler.SendPacketOpcode;
import bitbot.Constants;
import bitbot.util.encryption.output.PacketLittleEndianWriter;

/**
 *
 * @author z
 */
public class LoginPacket {

    public static final byte[] getHello(final short version, final byte[] sendIv, final byte[] recvIv) {
        final PacketLittleEndianWriter mplew = new PacketLittleEndianWriter();

        mplew.writeShort(version);
        mplew.writeMapleAsciiString(Constants.ServerClientMinorPatchVer);
        mplew.write(recvIv);
        mplew.write(sendIv);

        return mplew.getPacket();
    }

    public static final byte[] getPing() {
        final PacketLittleEndianWriter mplew = new PacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.PING.getValue());

        return mplew.getPacket();
    }

}
