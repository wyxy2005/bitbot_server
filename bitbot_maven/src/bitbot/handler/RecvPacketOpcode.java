package bitbot.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author z
 */
public enum RecvPacketOpcode implements WritableIntValueHolder {
    PONG
    ;
            
    private int code = -2;
    
    private RecvPacketOpcode() {
    }

    @Override
    public void setValue(int code) {
	this.code = code;
    }

    @Override
    public int getValue() {
	return code;
    }


    public static Properties getDefaultProperties() throws FileNotFoundException, IOException {
	Properties props = new Properties();
	FileInputStream fileInputStream = new FileInputStream("recvops.properties");

	props.load(fileInputStream);
	fileInputStream.close();
	return props;
    }

    public static final void reloadValues() {
	try {
	    ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
	} catch (IOException e) {
	    throw new RuntimeException("Failed to load sendops", e);
	}
    }

    public static final void LoadValues() {
	// static.
    }

    static {
	reloadValues();
    }
}
