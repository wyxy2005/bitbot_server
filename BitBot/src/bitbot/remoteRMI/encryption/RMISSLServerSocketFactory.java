/*
 * @(#)RMISSLServerSocketFactory.java	1.5 01/05/10
 *
 * Copyright 1995-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the following 
 * conditions are met:
 * 
 * -Redistributions of source code must retain the above copyright  
 * notice, this  list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright 
 * notice, this list of conditions and the following disclaimer in 
 * the documentation and/or other materials provided with the 
 * distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of 
 * contributors may be used to endorse or promote products derived 
 * from this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any 
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND 
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY 
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY 
 * DAMAGES OR LIABILITIES  SUFFERED BY LICENSEE AS A RESULT OF  OR 
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR 
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, 
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER 
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or 
 * intended for use in the design, construction, operation or 
 * maintenance of any nuclear facility. 
 */


package bitbot.remoteRMI.encryption;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

/**
 *
 * @author z
 */
public class RMISSLServerSocketFactory
	implements RMIServerSocketFactory, Serializable {

    @Override
    public ServerSocket createServerSocket(int port)
	throws IOException
	{ 
	    SSLServerSocketFactory ssf = null;
	    try {
		// set up key manager to do server authentication
		SSLContext ctx;
		KeyManagerFactory kmf;
		KeyStore ks;
		char[] passphrase = "asd41as5d4as54d5sa4d54asds5a!".toCharArray();

		ctx = SSLContext.getInstance("TLS");
		kmf = KeyManagerFactory.getInstance("SunX509");
		ks = KeyStore.getInstance("JKS");

		ks.load(new FileInputStream("bitbot_keystore.jks"), passphrase);
		kmf.init(ks, passphrase);
		ctx.init(kmf.getKeyManagers(), null, null);

		ssf = ctx.getServerSocketFactory();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	    return ssf.createServerSocket(port);
	}
}
