package bitbot.util.encryption.input;

import java.awt.Point;

/**
 * Provides an abstract interface for a little-endian stream of bytes.
 * 
 * @author Frz
 * @version 1.0
 * @since Revision 323
 */
public interface LittleEndianAccessor {

    /**
     * Reads a byte.
     *
     * @return The byte read.
     */
    byte readByte();

    /**
     * Reads a character.
     *
     * @return The character read.
     */
    char readChar();

    /**
     * Reads a short integer.
     *
     * @return The short integer read.
     */
    short readShort();

    int readShort2();

    /**
     * Reads a integer.
     *
     * @return The integer read.
     */
    int readInt();

    long readInt2();

    /**
     * Reads a long integer.
     *
     * @return The long integer read.
     */
    long readLong();

    /**
     * Skips ahead <code>num</code> bytes.
     *
     * @param num Number of bytes to skip ahead.
     */
    void skip(int num);

    /**
     * Reads a number of bytes.
     *
     * @param num The number of bytes to read.
     * @return The bytes read.
     */
    byte[] read(int num);

    /**
     * Reads a floating point integer.
     *
     * @return The floating point integer read.
     */
    float readFloat();

    /**
     * Reads a double-precision integer.
     *
     * @return The double-precision integer read.
     */
    double readDouble();

    /**
     * Reads an ASCII string.
     *
     * @return The string read.
     */
    String readAsciiString(int n);

    /**
     * Reads a null-terminated ASCII string.
     *
     * @return The string read.
     */
    String readNullTerminatedAsciiString();

    /**
     * Reads a MapleStory convention lengthed ASCII string.
     *
     * @return The string read.
     */
    String readMapleAsciiString();
    
    String readByteToStringAsciiString();

    /**
     * Reads a MapleStory Position information
     *
     * @return The Position read.
     */
    Point readPos();

    /**
     * Gets the number of bytes read so far.
     *
     * @return The number of bytes read as an long integer.
     */
    long getBytesRead();

    /**
     * Gets the number of bytes left for reading.
     *
     * @return The number of bytes left for reading as an long integer.
     */
    long available();

    /**
     * Prints the entire byte of packets remaining.
     *
     * @return
     */
    void Print(int count);
    void PrintAll();
}
