package bitbot.handler;

import bitbot.handler.channel.ChannelServer;
import bitbot.handler.channel.tasks.*;
import bitbot.server.Constants;
import bitbot.server.threads.MultiThreadExecutor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.simpleframework.http.Query;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

/**
 *
 * @author zheng hao
 */
public class ServerHTTPExchangeHandler implements Container {

    private Server server = null;
    private Container container = null;

    private ServerHTTPExchangeHandler() {
    }

    @Override
    public void handle(Request request, Response response) {
        String IPAddress = request.getClientAddress().getAddress().getHostAddress();
        String CloudFlareForwardAddress = request.getValue("X-Forwarded-For");

        //System.out.println("Session started by IP: " + IPAddress);
        //System.out.println("Session CloudFlare by IP: " + CloudFlareForwardAddress == null ? "" : CloudFlareForwardAddress);
        //System.out.println("Enforce cloudflare: " + ChannelServer.getInstance().isEnforceCloudFlareNetwork());
        boolean isCloudFlareAddress = CloudflareIPValidator.isCloudFlareIPAddress(IPAddress);
        //.out.println("IsCloudFlare: " + isCloudFlareAddress);

        // Source validation
        if (ChannelServer.getInstance().isEnforceCloudFlareNetwork()) { 
            if (CloudFlareForwardAddress == null || // No forward address yet we are enforcing "cloud-flare" only,
                    !isCloudFlareAddress) { // No a cloudflare address :D nice try h4x0r
                return;
            }
        }
        if (isCloudFlareAddress) {
            IPAddress = CloudFlareForwardAddress;
        }
        //System.out.println("EndUserIP : " + IPAddress);

        // Anti flood check
        if (AntiFloodValidator.CheckSpam(IPAddress)) {
            MultiThreadExecutor.submit(new SpamErrorTask(request, response));
            return;
        }

        // Query
        Query query = request.getQuery();
        String queryType = query.get("Type"); // Chart  

        //http://www.simpleframework.org/doc/tutorial/tutorial.php
        Runnable r; // ensure that r is always not null
        if (queryType == null) {
            r = new EchoClientTask(request, response);
        } else {
            // http://127.0.0.1:8080/?Type=Chart
            switch (queryType) {
                case "Chart": {
                    r = new ChartTask(request, response, query);
                    break;
                }
                case "CandlestickChart": {
                    r = new ChartTask_Candlestick(request, response, query);
                    break;
                }
                case "BackTest": {
                    r = new BacktestTask(request, response, query);
                    break;
                }
                case "EMA": {
                    r = new ExponentialMovingAverageTask(request, response, query);
                    break;
                }
                case "Swaps": {
                    r = new SwapTask(request, response, query);
                    break;
                }
                default: {
                    r = new EchoClientTask(request, response);
                    break;
                }
            }
        }
        MultiThreadExecutor.submit(r);
    }

    public static ServerHTTPExchangeHandler Connect() throws Exception {
        System.out.println("[Info] Establishing server HTTP/HTTPS container");
        
        ServerHTTPExchangeHandler serverhandler = new ServerHTTPExchangeHandler();
        serverhandler.container = serverhandler;
        serverhandler.server = new ContainerServer(serverhandler.container);

        Server server = new ContainerServer(serverhandler);
        
        Connection connection = new SocketConnection(server);
        
        // Init HTTP 
       /* System.out.println("[Info] Starting HTTP server at port " + Constants.SocketPort);
        
        SocketAddress address = new InetSocketAddress(Constants.SocketPort);
        connection.connect(address);
        */
        System.out.println("[Info] Starting HTTP Legacy server at port " + Constants.SocketPortLegacy);
        
        SocketAddress addressL = new InetSocketAddress(Constants.SocketPortLegacy);
        connection.connect(addressL);
        
        // Init HTTPs
        System.out.println("[Info] Starting HTTPs server at port " + Constants.SocketPort_HTTPs);
        
        SocketAddress address_secure = new InetSocketAddress(Constants.SocketPort_HTTPs);
        connection.connect(address_secure, GenerateKeyStore());
        
        return serverhandler;
    }
    
    private static SSLContext GenerateKeyStore() throws Exception {
        // password = asd41as5d4as54d5sa4d54asds5a!
        final String KeyStorePassword = "asd41as5d4as54d5sa4d54asds5a!";
        
        KeyManagerFactory km = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore serverKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream keystoreFile = new FileInputStream("bitbot_keystore.jks")) {
            serverKeystore.load(keystoreFile, KeyStorePassword.toCharArray());
        }
        km.init(serverKeystore, KeyStorePassword.toCharArray());

        // asdas2d1214uytuytklhmkflm4564
        final String CAKeyStorePassword = "asdas2d1214uytuytklhmkflm4564";
        
        TrustManagerFactory tm = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore caKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream caCertFile = new FileInputStream("ca_keystore.jks")) {
            caKeystore.load(caCertFile, CAKeyStorePassword.toCharArray());
        }
        tm.init(caKeystore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(km.getKeyManagers(), tm.getTrustManagers(), null);
        
        return sslContext;
    }

    public void Disconnect() {
        try {
            server.stop();
        } catch (IOException exp) {
            // doesnt matter, we are fuked anyway
        }
    }
}
