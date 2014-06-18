package bitbot;

import bitbot.handler.channel.ChannelServer;
import bitbot.handler.world.WorldServer;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import bitbot.util.MT4CVSReader;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author z
 */
public class Main {

    public static void main(String args[]) throws Exception {
        String command = args[0];
        switch (command) {
            case "startWorld": {
                 WorldServer.getInstance().initializeWorldServer();
                break;
            }
            case "start": {
                /*
                 *  fix for
                 *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
                 *       sun.security.validator.ValidatorException:
                 *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
                 *               unable to find valid certification path to requested target
                 */
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                    }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // Create all-trusting host name verifier
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };
                // Install the all-trusting host verifier
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
                /*
                 * end of the fix
                 */

                // Start services
                MultiThreadExecutor.start();
                TimerManager.start();

                ChannelServer.getInstance().initializeChannelServer();

                /*Scanner sc = new Scanner(System.in);
                 System.out.println(":::::::: Enter commands ::::::::");
                 while (sc.hasNextLine()) {
                 String NextLine = sc.nextLine();
                 String[] input = NextLine.split(" ");
            
                 switch (input[0]) {
                 case "-benchmarkSelect": {
                 ProcessCommand(input);
                 break;
                 }
                 }
                 }*/
                break;
            }
            case "readMT4Data": {
                if (args.length <= 1) {
                    System.out.println("Please specify the .CVS path.");
                    return;
                }
                String CVSFile = args[1];

                MT4CVSReader.ReadCVSFile(CVSFile);
            }
        }
    }

    private static void ProcessCommand(String[] input) {

    }
}
