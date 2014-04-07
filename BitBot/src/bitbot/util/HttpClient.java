package bitbot.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author z
 */
public class HttpClient {

    public static String httpGet(String URL, String parameters) {
        BufferedReader in = null;
        try {
            URL obj = new URL(URL + parameters);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");

            // Send post request
            con.setDoOutput(false);

            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'POST' request to URL : " + obj.toString());
            //System.out.println("Post parameters : " + PostStr);
            //System.out.println("Response Code : " + responseCode);

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
            //exp.printStackTrace();
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
    
    public static String httpsGet(String URL, String parameters) {
        BufferedReader in = null;
        try {
            URL obj = new URL(URL + parameters);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");

            // Send post request
            con.setDoOutput(false);

            int responseCode = con.getResponseCode();
            //System.out.println("\nSending 'POST' request to URL : " + obj.toString());
            //System.out.println("Post parameters : " + PostStr);
            //System.out.println("Response Code : " + responseCode);

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
            //exp.printStackTrace();
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
