package bitbot.util.encryption.output;

import bitbot.util.StringUtil;
import bitbot.util.encryption.HexTool;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * Provides a generic writer of a little-endian sequence of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public class GenericLittleEndianWriter implements LittleEndianWriter {

    // See http://java.sun.com/j2se/1.4.2/docs/api/java/nio/charset/Charset.html
    private static final Charset ASCII = Charset.forName("UTF-8"); // ISO-8859-1, UTF-8
    private ByteArrayOutputStream bos;
 //   public int pos = 0;

    /**
     * Class constructor - Protected to prevent instantiation with no arguments.
     */
    protected GenericLittleEndianWriter() {
	// Blah!
    }

    /**
     * Sets the byte-output stream for this instance of the object.
     *
     * @param bos The new output stream to set.
     */
    protected void setByteOutputStream(ByteArrayOutputStream bos) {
	this.bos = bos;
    }
    
    protected ByteArrayOutputStream getBao() {
	return bos;
    }

    /**
     * Class constructor - only this one can be used.
     *
     * @param bos The stream to wrap this objecr around.
     */
    public GenericLittleEndianWriter(ByteArrayOutputStream bos) {
	this.bos = bos;
    }

    /**
     * Write the number of zero bytes
     *
     * @param b The bytes to write.
     */
    @Override
    public void writeZeroBytes(int i) {
	for (int x = 0; x < i; x++) {
	    write((byte) 0);
	}
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    @Override
    public void write(byte[] b) {
	for (int x = 0; x < b.length; x++) {
	    write(b[x]);
	}
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    @Override
    public void write(byte b) {
	bos.write(b);
//	pos++;
    }

    /**
     * Write a byte in integer form to the stream.
     *
     * @param b The byte as an <code>Integer</code> to write.
     */
    @Override
    public void write(int b) {
	bos.write((byte) b);
//	pos++;
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    @Override
    public void writeShort(int i) {
	write((byte) (i & 0xFF));
	write((byte) ((i >>> 8) & 0xFF));
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    @Override
    public void writeInt(int i) {
	write((byte) (i & 0xFF));
	write((byte) ((i >>> 8) & 0xFF));
	write((byte) ((i >>> 16) & 0xFF));
	write((byte) ((i >>> 24) & 0xFF));
    }

    @Override
    public void writeInt(long i) {
	write((byte) (i & 0xFF));
	write((byte) ((i >>> 8) & 0xFF));
	write((byte) ((i >>> 16) & 0xFF));
	write((byte) ((i >>> 24) & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeAsciiString(String s) {
	write(s.getBytes(ASCII));
    }

    @Override
    public void writeAsciiString(String s, int max) {
	write(s.getBytes(ASCII));
	for (int i = s.length(); i < max; i++) {
	    write(0);
	}
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    @Override
    public void writeMapleAsciiString(String s) {
	writeShort((short) s.length());
	writeAsciiString(s);
    }

    @Override
    public void writeMapleAsciiString(String s, int max, char end) {
	String mod = StringUtil.getRightPaddedStr(s, end, max);
	
	writeShort((short)mod.length());
	writeAsciiString(mod);
    }

    /**
     * Writes a null-terminated ASCII string to the stream.
     *
     * @param s The ASCII string to write.
     */
    @Override
    public void writeNullTerminatedAsciiString(String s) {
	writeAsciiString(s);
	write(0);
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    @Override
    public void writePos(Point s) {
	writeShort(s.x);
	writeShort(s.y);
    }

    /**
     * Write a long integer to the stream.
     * @param l The long integer to write.
     */
    @Override
    public void writeLong(long l) {
	write((byte) (l & 0xFF));
	write((byte) ((l >>> 8) & 0xFF));
	write((byte) ((l >>> 16) & 0xFF));
	write((byte) ((l >>> 24) & 0xFF));
	write((byte) ((l >>> 32) & 0xFF));
	write((byte) ((l >>> 40) & 0xFF));
	write((byte) ((l >>> 48) & 0xFF));
	write((byte) ((l >>> 56) & 0xFF));
    }
    
    @Override
    public void writeFloat(float l) {
        this.writeInt(Float.floatToIntBits(l));
    }
    
    @Override
    public void writeDouble(double l) {
        this.writeLong(Double.doubleToLongBits(l));
    }

    @Override
    public void writeHex(String hex) {
	write(HexTool.getByteArrayFromHexString(hex));
    }
}
