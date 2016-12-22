package bitbot.remoteRMI.encryption;

import bitbot.util.encryption.XorSocket;
import java.io.*;
import java.net.*;
import java.rmi.server.*;

/**
 *
 * @author z
 */
public class XorClientSocketFactory implements RMIClientSocketFactory, Serializable {

    private final byte pattern;

    public XorClientSocketFactory() {
	this.pattern = 0x5f;
    }
    
    public XorClientSocketFactory(byte pattern) {
	this.pattern = pattern;
    }
    
    @Override
    public Socket createSocket(String host, int port)
	throws IOException
    {
	return new XorSocket(host, port, pattern);
    }
    
    @Override
    public int hashCode() {
	return (int) pattern;
    }

    @Override
    public boolean equals(Object obj) {
	return (getClass() == obj.getClass() &&
		pattern == ((XorClientSocketFactory) obj).pattern);
    }
}