package bitbot.remoteRMI.encryption;

import bitbot.util.encryption.XorServerSocket;
import java.io.*;
import java.net.*;
import java.rmi.server.*;

/**
 *
 * @author z
 */
public class XorServerSocketFactory implements RMIServerSocketFactory {

    private final byte pattern;
    
    public XorServerSocketFactory() {
	this.pattern = 0x5f;
    }
    
    public XorServerSocketFactory(byte pattern) {
	this.pattern = pattern;
    }
    
    @Override
    public ServerSocket createServerSocket(int port)
	throws IOException
    {
	return new XorServerSocket(port, pattern);
    }
    
    @Override
    public int hashCode() {
	return (int) pattern;
    }

    @Override
    public boolean equals(Object obj) {
	return (getClass() == obj.getClass() &&
		pattern == ((XorServerSocketFactory) obj).pattern);
    }

}