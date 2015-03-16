package bitbot.handler.mina;


import bitbot.client.Client;
import bitbot.Constants;
import bitbot.util.encryption.AESOFB;
import bitbot.util.encryption.CustomEncryption;
import java.util.concurrent.locks.Lock;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class MaplePacketEncoder implements ProtocolEncoder {

    @Override
    public void encode(final IoSession session, final Object message, final ProtocolEncoderOutput out) throws Exception {
	final Client client = (Client) session.getAttribute(Constants.CLIENT_KEY);

	if (client != null) {
	    final Lock mutex = client.getLock();

	    mutex.lock();
	    try {
		final AESOFB send_crypto = client.getSendCrypto();

		final byte[] inputInitialPacket = ((byte[]) message);
		final byte[] unencrypted = new byte[inputInitialPacket.length];
		System.arraycopy(inputInitialPacket, 0, unencrypted, 0, inputInitialPacket.length); // Copy the input > "unencrypted"

		final byte[] header_Expandedbyte = send_crypto.getPacketHeaderEx(unencrypted.length);

		CustomEncryption.encryptData(unencrypted); // Encrypting Data
		send_crypto.crypt(unencrypted); // Crypt it with IV

		System.arraycopy(unencrypted, 0, header_Expandedbyte, 4, unencrypted.length);
		out.write(IoBuffer.wrap(header_Expandedbyte));
	    } finally {
		mutex.unlock();
	    }
	} else { // no client object created yet, send unencrypted (hello)
	    out.write(IoBuffer.wrap((byte[]) message));
	}
    }

    @Override
    public void dispose(IoSession session) throws Exception {
	// nothing to do
    }
}
