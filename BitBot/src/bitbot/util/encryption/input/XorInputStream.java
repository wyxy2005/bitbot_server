package bitbot.util.encryption.input;

import bitbot.util.encryption.BitTools;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author z
 */
public class XorInputStream extends FilterInputStream {

    /*
     * The byte being used to "decrypt" each byte of data.
     */
    private byte pattern;

    /*
     * Constructs an input stream that uses the specified pattern
     * to "decrypt" each byte of data.
     */
    public XorInputStream(InputStream in, byte pattern) {
        super(in);
        this.pattern = pattern;
    }

    /*
     * Reads in a byte and xor's the byte with the pattern.
     * Returns the byte.
     */
    @Override
    public int read() throws IOException {
        int b = in.read();
        //If not end of file or an error, truncate b to one byte
        if (b != -1) 
            b = (b ^ pattern) & 0xFF;

        //pattern++; // some randomization
        
        return b;
    }

    /*
     * Reads up to len bytes
     */
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int numBytes = in.read(b, off, len);

        if (numBytes <= 0) {
            return numBytes;
        }

        for (int i = 0; i < numBytes; i++) {
            byte boff = b[off + i];
            b[off + i] = (byte) ((boff ^ pattern) & 0xFF);
            
            //pattern++; // some randomization
        }
        return numBytes;
    }
}
