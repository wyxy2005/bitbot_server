package bitbot.util;

import bitbot.Constants;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
            URL obj = new URL(URL + (parameters != null ? parameters : ""));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", Constants.IE11_UserAgent);

            // Send post request
            con.setDoOutput(false);
            con.setReadTimeout(10000); // timeout of 10 seconds

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
            URL obj = new URL(URL + (parameters != null ? parameters : ""));
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", Constants.Chrome_UserAgent_Nov82016);

            // Send post request
            con.setDoOutput(false);
            con.setReadTimeout(10000);

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

    public static String httpsPost(String URL, String postmsg) {
        BufferedReader in = null;
        BufferedWriter out = null;
        try {
            URL obj = new URL(URL);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", Constants.IE11_UserAgent);

            con.setDoOutput(true);
            con.setReadTimeout(10000);

            // Post msg
            out = new BufferedWriter(
                    new OutputStreamWriter(con.getOutputStream()));
            out.write(postmsg);

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
            exp.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException exp) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException exp) {
                }
            }
        }
        return null;
    }
}
