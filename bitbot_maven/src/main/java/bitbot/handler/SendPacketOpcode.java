package bitbot.handler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author z
 */
public enum SendPacketOpcode implements WritableIntValueHolder {
    PING
    ;

    private int code = -2;

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
	FileInputStream fileInputStream = new FileInputStream("sendops.properties");
	props.load(fileInputStream);
	fileInputStream.close();
	return props;
    }

    static {
	reloadValues();
    }

    public static final void LoadValues() {
	// static.
    }

    public static final void reloadValues() {
	try {
	    ExternalCodeTableGetter.populateValues(getDefaultProperties(), values());
	} catch (IOException e) {
	    throw new RuntimeException("Failed to load sendops", e);
	}
    }
}
