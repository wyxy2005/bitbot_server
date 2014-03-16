package bitbot.handler.channel;

import bitbot.handler.ServerHandler;
import bitbot.server.Constants;
import bitbot.server.news.NewsCacheTask;
import bitbot.tickers.TickerCacheTask;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author z
 */
public class ChannelServer {

    public static final ChannelServer ch = new ChannelServer();
    private ServerHandler serverHandler = null;
    private TickerCacheTask tickerTask = null;
    private NewsCacheTask newsTask = null;
    
    // Properties
    private static Properties props = null;
    private static boolean 
            EnforceCloudFlareNetwork = false;

    // Etc
    private final List<String> CachingCurrencyPair = new ArrayList();

    private ChannelServer() {

    }

    public static ChannelServer getInstance() {
        if (ch.serverHandler == null) {
            ch.initializeChannelServer();
        }
        return ch;
    }

    public void initializeChannelServer() {
        if (serverHandler == null) {
            try {
                // Init properties
                if (props == null) {
                    props = new Properties();
                    try (FileReader is = new FileReader("server.properties")) {
                        props.load(is);
                    }
                }
                EnforceCloudFlareNetwork = Boolean.parseBoolean(props.getProperty("server.EnforceCloudFlareNetwork"));
                
                // End
                serverHandler = ServerHandler.Connect();

                LoadCurrencyPairTables(false);
                
                tickerTask = new TickerCacheTask(); // init automatically
                newsTask = new NewsCacheTask();
            } catch (Exception exp) {
                System.out.println("Failed to start Channel server, please ensure the port is unused.");
                exp.printStackTrace();

                if (serverHandler != null) {
                    serverHandler.Disconnect();
                }
                serverHandler = null;
            }
        }
    }

    public void LoadCurrencyPairTables(boolean reload) {
        if (!CachingCurrencyPair.isEmpty() && !reload)
            return;
        
        File f = new File(Constants.CurrencyPairFile);
        if (!f.exists()) {
            try {
                f.createNewFile();
                try (FileWriter Writer = new FileWriter(Constants.CurrencyPairFile, false)) {
                    Writer.append(Constants.DefaultCurrencyPair);
                }
            } catch (IOException ioe) {
                System.out.println(ioe.toString() + " Error accessing file system to create Blocked IP list.");
            }
        }
        try (FileReader Reader = new FileReader(Constants.CurrencyPairFile)) {
            try (BufferedReader bufReader = new BufferedReader(Reader)) {
                String str = bufReader.readLine();

                if (str != null) {
                    String[] SourcePairs = str.split("---");
                    CachingCurrencyPair.clear();
                    
                    for (String SourcePair : SourcePairs) 
                    {
                        CachingCurrencyPair.add(SourcePair);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString() + " Error accessing Blocked IP list.");
        }
    }

    public List<String> getCachingCurrencyPair() {
        return CachingCurrencyPair;
    }
    
    public TickerCacheTask getTickerTask() {
        return tickerTask;
    }
    
    public NewsCacheTask getNewsTask() {
        return newsTask;
    }
    
    public boolean isEnforceCloudFlareNetwork() {
        return EnforceCloudFlareNetwork;
    }
}
