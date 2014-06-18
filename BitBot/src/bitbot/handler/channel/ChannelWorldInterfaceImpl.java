package bitbot.handler.channel;

import bitbot.remoteRMI.ChannelWorldInterface;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

/**
 *
 * @author z
 */
public class ChannelWorldInterfaceImpl extends UnicastRemoteObject implements ChannelWorldInterface {
    private static final long serialVersionUID = 781525689948864492L;
    private ChannelServer server;
    
    public ChannelWorldInterfaceImpl() throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
    }

    public ChannelWorldInterfaceImpl(ChannelServer server) throws RemoteException {
	super(0, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
	this.server = server;
    }

    
    @Override
    public void setChannelId(byte id) throws RemoteException {
        
    }

    @Override
    public byte getChannelId() throws RemoteException{
        return 0;
    }
    
    @Override
    public String getIP() throws RemoteException {
        return "";
    }

    @Override
    public void broadcastMessage(byte[] message) throws RemoteException{
         
    }
}
