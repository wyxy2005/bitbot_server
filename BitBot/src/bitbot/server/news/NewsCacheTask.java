package bitbot.server.news;

import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class NewsCacheTask {

    private static final int News_CacheRefreshTime_Seconds = 120;
    private LoggingSaveRunnable runnable;
    private final ArrayList<NewsItem> list_News;
    private final ReentrantLock mutex_news = new ReentrantLock();

    public NewsCacheTask() {
        this.list_News = new ArrayList<>();
        StartScheduleTask();
    }

    public void StartScheduleTask() {
        runnable = TimerManager.register(new NewsCacheTaskRunnable(), News_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE);
    }

    public void CancelAllTask() {
        if (runnable != null) {
            runnable.getSchedule().cancel(true);
            runnable = null;
        }
    }

    public ArrayList<NewsItem> getListNews() {
        return new ArrayList(list_News);
    }

    public class NewsCacheTaskRunnable implements Runnable {

        public NewsCacheTaskRunnable() {
        }

        @Override
        public void run() {
            System.out.println("Caching News record");

            // new NewsItem
            String news_Bing_ret = NewsCacheTask.GetHttp_Bing();
            String news_coindesk_ret = NewsCacheTask.GetHttp("http://feeds.feedburner.com/CoinDesk");

            if (news_Bing_ret != null && news_coindesk_ret != null) { // Only override if we got both data
                // Read Bing Input
                JSONParser parser = new JSONParser();
                try {
                    JSONObject BdiGenericBingResponse10 = (JSONObject) ((JSONObject) parser.parse(news_Bing_ret)).get("BdiGeneric_BingResponse_1_0");
                    JSONObject Responses = (JSONObject) ((JSONArray) BdiGenericBingResponse10.get("Responses")).get(0);
                    JSONObject ResultSet = (JSONObject) ((JSONArray)Responses.get("ResultSet")).get(0);
                    JSONArray Results = (JSONArray) ResultSet.get("Results");
                    
                    for (Object obj_ : Results) {
                        JSONObject obj = (JSONObject) obj_;
                        
                        JSONObject article = (JSONObject) obj.get("Article");
                        
                        if (article != null) {
                            Object URLLink = ((JSONObject) article.get("Url")).get("UrlLink");
                            if (URLLink != null) {
                                System.out.println(URLLink.toString());
                            } 
                        }
                    }
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }
    }

    private static String GetHttp(String Uri) {
        BufferedReader in = null;
        try {
            URL obj = new URL(Uri);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");

            // Send post request
            con.setDoOutput(false);

            int responseCode = con.getResponseCode();

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();

        } catch (IOException exp) {
            exp.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException exp) {
                }
            }
        }
        return null;
    }

    private static String GetHttp_Bing() {
        BufferedReader in = null;
        try {
            URL obj = new URL(String.format("http://appex.bing.com/news?a=NewsArticlesByTopic_2.1&appid=C98EA5B0842DBB9405BBF071E1DA7651077B1B5B&q=%s&q2=0&q3=20&q4=0&setmkt=EN-US&FORM=APXN05", "bitcoin"));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");
            //con.setRequestProperty("Date", Constants.Server_UserAgent);
            con.setRequestProperty("Accept", "*/*");
            con.setRequestProperty("Host", "appex.bing.com");
            con.setRequestProperty("User-Agent", "X-Client/AppexWinPhone8NewsApp X-Client-AppVersion/1.1.0.49");

            // Send post request
            con.setDoOutput(false);

            int responseCode = con.getResponseCode();

            in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            return response.toString();

        } catch (IOException exp) {
            exp.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException exp) {
                }
            }
        }
        return null;
    }
}
