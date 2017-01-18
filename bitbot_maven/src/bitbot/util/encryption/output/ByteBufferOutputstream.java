package bitbot.util.encryption.output;

import org.apache.mina.core.buffer.IoBuffer;

/**
 * Uses a bytebuffer as an underlying storage method to hold a stream of bytes.
 *
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class ByteBufferOutputstream implements ByteOutputStream {

    private IoBuffer bb;

    /**
     * Class constructor - Wraps this instance around ByteBuffer <code>bb</code>
     *
     * @param bb The <code>org.apache.mina.common.ByteBuffer</code> to wrap
     *            this stream around.
     */
    public ByteBufferOutputstream(IoBuffer bb) {
	super();
	this.bb = bb;
    }

    /**
     * Writes a byte to the underlying buffer.
     *
     * @param b The byte to write.
     * @see org.apache.mina.common.ByteBuffer#put(byte)
     */
    @Override
    public void writeByte(byte b) {
	bb.put(b);
    }
}