package bitbot.util.encryption.output;

import java.io.*;

/**
 *
 * @author z
 */
public class XorOutputStream extends FilterOutputStream {

    /*
     * The byte used to "encrypt" each byte of data.
     */
    private byte pattern;

    /* 
     * Constructs an output stream that uses the specified pattern
     * to "encrypt" each byte of data.
     */
    public XorOutputStream(OutputStream out, byte pattern) {
        super(out);
        this.pattern = pattern;
    }

    /*
     * XOR's the byte being written with the pattern
     * and writes the result.  
     */
    @Override
    public void write(int b) throws IOException {
        out.write((b ^ pattern) & 0xFF);
        
        //pattern++; // some randomization
    }
}
