package bitbot;

import bitbot.handler.CloudflareIPValidator;
import bitbot.handler.channel.ChannelServer;
import bitbot.handler.world.WorldServer;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import bitbot.util.BitcoinWisdomReader;
import bitbot.util.MT4CVSReader;
import bitbot.util.encryption.CustomXorEncryption;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.lang3.time.DateUtils;

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
            case "readBitcoinWisdomData": {
                if (args.length <= 1) {
                    System.out.println("Please specify .txt path to json file.");
                    return;
                }
                String txtJsonFile = args[1];
                String sqlPat = args[2];
                boolean showdebugOnly = Boolean.parseBoolean(args[3]);

                BitcoinWisdomReader.ReadJsonFile(txtJsonFile, sqlPat, showdebugOnly);
                break;
            }
            case "readMT4Data": {
                if (args.length <= 1) {
                    System.out.println("Please specify the .CVS path.");
                    return;
                }
                String CVSFile = args[1];

                MT4CVSReader.ReadCVSFile(CVSFile);
                break;
            }
            case "testtime": {
                int intervalMinutes = Integer.parseInt(args[1]);
                
                final Calendar dtCal = Calendar.getInstance();
                dtCal.setTimeInMillis(System.currentTimeMillis());

                int truncateField = -1;
                if (intervalMinutes < 60) {
                    // below 1 hour
                    truncateField = Calendar.HOUR; // {Calendar.HOUR_OF_DAY, Calendar.HOUR},
                } else if (intervalMinutes < 60 * 24) {
                    // below 1 day
                    truncateField = Calendar.DATE; // {Calendar.DATE, Calendar.DAY_OF_MONTH, Calendar.AM_PM, Calendar.DAY_OF_YEAR, Calendar.DAY_OF_WEEK, Calendar.DAY_OF_WEEK_IN_MONTH */
                } else if (intervalMinutes < 60 * 24 * 30) {
                    // below 30 days
                    truncateField = Calendar.MONTH;
               } else if (intervalMinutes < 60 * 24 * 30 * 12 * 100) {
                    // below 100 years
                    truncateField = Calendar.YEAR;
                } else {
                   // wtf
                    truncateField = Calendar.ERA;
               }
                long LastUsedTime = DateUtils.truncate(dtCal, truncateField).getTimeInMillis();
                
                final Calendar dtCal_final = Calendar.getInstance();
                dtCal_final.setTimeInMillis(LastUsedTime);
                System.out.println(dtCal_final.getTime().toString());
                break;
            }
            default: {
                System.out.println(CustomXorEncryption.custom_xor_encrypt("test", 1015561));
                break;
            }
        }
    }

    private static void ProcessCommand(String[] input) {

    }
}
