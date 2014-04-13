package bitbot.handler.mina;

import bitbot.client.Client;
import bitbot.server.Constants;
import bitbot.util.encryption.AESOFB;
import bitbot.util.encryption.CustomEncryption;

import java.util.concurrent.locks.Lock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PacketDecoder extends CumulativeProtocolDecoder {

    public static final String DECODER_STATE_KEY = PacketDecoder.class.getName() + ".STATE";

    public static class DecoderState {

	public int packetlength = -1;
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
	final DecoderState decoderState = (DecoderState) session.getAttribute(DECODER_STATE_KEY);

	final Client client = (Client) session.getAttribute(Constants.CLIENT_KEY);	
	final Lock mutex = client.getLock();
	mutex.lock();
	try {
	    if (decoderState.packetlength == -1) {
		if (in.remaining() < 4) {
		    return false;
		}
		final int packetHeader = in.getInt();
		if (!client.getReceiveCrypto().checkPacket(packetHeader)) {
		    session.close(true);
		    return false;
		}
		decoderState.packetlength = AESOFB.getPacketLength(packetHeader);
	    }
	    if (in.remaining() < decoderState.packetlength) {
		return false;
	    }
	    final byte decryptedPacket[] = new byte[decoderState.packetlength];
	    in.get(decryptedPacket, 0, decoderState.packetlength);
	    decoderState.packetlength = -1;

	    client.getReceiveCrypto().crypt(decryptedPacket);
	    CustomEncryption.decryptData(decryptedPacket);
	    out.write(decryptedPacket);
//	    System.out.println("Pending : " + session.getScheduledWriteMessages() + " output : " + session.getReadMessagesThroughput() + " " + session.getWrittenMessagesThroughput());
	} finally {
	    mutex.unlock();
	}
	return true;
    }
}
