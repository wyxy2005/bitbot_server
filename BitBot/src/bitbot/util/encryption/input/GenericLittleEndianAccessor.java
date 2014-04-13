package bitbot.util.encryption.input;

import bitbot.util.encryption.HexTool;
import java.awt.Point;
import java.io.ByteArrayOutputStream;

/**
 * Provides a generic interface to a Little Endian stream of bytes.
 * 
 * @version 1.0
 * @author Frz
 * @since Revision 323
 */
public class GenericLittleEndianAccessor implements LittleEndianAccessor {

    private final ByteInputStream bs;

    /**
     * Class constructor - Wraps the accessor around a stream of bytes.
     *
     * @param bs The byte stream to wrap the accessor around.
     */
    public GenericLittleEndianAccessor(final ByteInputStream bs) {
	this.bs = bs;
    }

    /**
     * Read a single byte from the stream.
     *
     * @return The byte read.
     * @see net.sf.odinms.tools.data.input.ByteInputStream#readByte
     */
    @Override
    public final byte readByte() {
	return (byte) bs.readByte();
    }

    /**
     * Reads an integer from the stream.
     *
     * @return The integer read.
     */
    @Override
    public final int readInt() {
	final int byte1 = bs.readByte();
	final int byte2 = bs.readByte();
	final int byte3 = bs.readByte();
	final int byte4 = bs.readByte();
	return (byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1;
    }

    @Override
    public final long readInt2() {
	final int byte1 = bs.readByte();
	final int byte2 = bs.readByte();
	final int byte3 = bs.readByte();
	final int byte4 = bs.readByte();
	return (long) ((byte4 << 24) + (byte3 << 16) + (byte2 << 8) + byte1);
    }

    /**
     * Reads a short integer from the stream.
     *
     * @return The short read.
     */
    @Override
    public final short readShort() {
	final int byte1 = bs.readByte();
	final int byte2 = bs.readByte();
	return (short) ((byte2 << 8) + byte1);
    }

    @Override
    public final int readShort2() {
	final int byte1 = bs.readByte();
	final int byte2 = bs.readByte();
	return ((byte2 << 8) + byte1);
    }

    /**
     * Reads a single character from the stream.
     *
     * @return The character read.
     */
    @Override
    public final char readChar() {
	return (char) readShort();
    }

    /**
     * Reads a long integer from the stream.
     *
     * @return The long integer read.
     */
    @Override
    public final long readLong() {
	final int byte1 = bs.readByte();
	final int byte2 = bs.readByte();
	final int byte3 = bs.readByte();
	final int byte4 = bs.readByte();
	final int byte5 = bs.readByte();
	final int byte6 = bs.readByte();
	final int byte7 = bs.readByte();
	final int byte8 = bs.readByte();

	return (byte8 << 56) + (byte7 << 48) + (byte6 << 40) + (byte5 << 32) + (byte4 << 24) + (byte3 << 16)
		+ (byte2 << 8) + byte1;
    }

    /**
     * Reads a floating point integer from the stream.
     *
     * @return The float-type integer read.
     */
    @Override
    public final float readFloat() {
	return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a double-precision integer from the stream.
     *
     * @return The double-type integer read.
     */
    @Override
    public final double readDouble() {
	return Double.longBitsToDouble(readLong());
    }

    /**
     * Reads an ASCII string from the stream with length <code>n</code>.
     *
     * @param n Number of characters to read.
     * @return The string read.
     */
    public final String readAsciiString(final int n) {
	final char ret[] = new char[n];
	byte c;
	for (int i = 0; i < n; i++) {
	    c = readByte();
	    ret[i] = (c >= 32 && c <= 126) ? ((char) c) : ((char) 42);
	}
	return new String(ret);
    }

    /**
     * Reads a null-terminated string from the stream.
     *
     * @return The string read.
     */
    public final String readNullTerminatedAsciiString() {
	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	byte b = 1;
	while (b != 0) {
	    b = readByte();
	    baos.write(b);
	}
	final byte[] buf = baos.toByteArray();
	final char[] chrBuf = new char[buf.length];
	for (int x = 0; x < buf.length; x++) {
	    chrBuf[x] = (char) buf[x];
	}
	return new String(chrBuf);
    }

    /**
     * Gets the number of bytes read from the stream so far.
     *
     * @return A long integer representing the number of bytes read.
     * @see net.sf.odinms.tools.data.input.ByteInputStream#getBytesRead()
     */
    public final long getBytesRead() {
	return bs.getBytesRead();
    }

    /**
     * Reads a MapleStory convention lengthed ASCII string.
     * This consists of a short integer telling the length of the string,
     * then the string itself.
     *
     * @return The string read.
     */
    @Override
    public final String readMapleAsciiString() {
	return readAsciiString(readShort());
    }
    
    @Override
    public final String readByteToStringAsciiString() {
	int len = readShort();
	StringBuilder sb = new StringBuilder();
	
	for (int i = 0; i < len; i++) {
	    sb.append(Integer.toHexString(readByte()));
	}
	return sb.toString();
    }

    /**
     * Reads a MapleStory Position information.
     * This consists of 2 short integer.
     *
     * @return The Position read.
     */
    @Override
    public final Point readPos() {
	final int x = readShort();
	final int y = readShort();
	return new Point(x, y);
    }

    /**
     * Reads <code>num</code> bytes off the stream.
     *
     * @param num The number of bytes to read.
     * @return An array of bytes with the length of <code>num</code>
     */
    @Override
    public final byte[] read(final int num) {
	byte[] ret = new byte[num];
	for (int x = 0; x < num; x++) {
	    ret[x] = readByte();
	}
	return ret;
    }

    /**
     * Skips the current position of the stream <code>num</code> bytes ahead.
     *
     * @param num Number of bytes to skip.
     */
    @Override
    public void skip(final int num) {
	for (int x = 0; x < num; x++) {
	    readByte();
	}
    }

    /**
     * @see net.sf.odinms.tools.data.input.ByteInputStream#available
     */
    @Override
    public final long available() {
	return bs.available();
    }

    /**
     * @see java.lang.Object#toString
     */
    @Override
    public final String toString() {
	return bs.toString();
    }

    @Override
    public void Print(int count) {
	if (bs.available() > 0) {
	    System.out.println(HexTool.toString(read(Math.min((int) available(), count))));
	}
    }

    @Override
    public void PrintAll() {
	if (bs.available() > 0) {
	    System.out.println(HexTool.toString(read((int) available())));
	}
    }
}
