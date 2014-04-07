package bitbot.external;

import bitbot.server.Constants;
import bitbot.server.mssql.DatabaseConnection;
import bitbot.cache.tickers.TickerItemData;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Using Windows Azure API to access instead, that way we can avoid security
 * issue entirely with MSSQL
 *
 * @author z
 */
public class AzureBitBot {

    public static boolean btce_Select_Graph_Data(String ExchangeSite, String currencyPair, int depthSelection, int hoursSelection, long start_server_time, ArrayList<TickerItemData> list_BTCe2) {
        // currencyPair = eg: btc_usd
     /*   String parameters = String.format("nonce=%d&currencypair=%s&depth=%d&hours=%d&start_server_time=%d&exchangesite=%s",
         System.currentTimeMillis(), currencyPair, depthSelection, hoursSelection, start_server_time, ExchangeSite);

         return post("https://bitcoinbot.azure-mobile.net/api/btce_select_graph_data?", parameters, "");
         */

        String tableName = ExchangeSite + "_price_" + currencyPair;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT TOP " + depthSelection + "  high, low, buy, vol, vol_cur, server_time FROM BitCoinBot." + tableName + " WHERE __createdAt < dateadd(hh, + " + hoursSelection + ", getdate()) AND server_time > ? ORDER BY server_time ASC;");
            ps.setLong(1, start_server_time);

            rs = ps.executeQuery();

            if (rs != null) {
                while (rs.next()) {
                    TickerItemData item = new TickerItemData(rs);

                    list_BTCe2.add(item);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean btce_Select_Graph_Data_AzureMobileAPI(String ExchangeSite, String currencyPair, int depthSelection, int hoursSelection, long start_server_time, ArrayList<TickerItemData> list_BTCe2) {
        // currencyPair = eg: btc_usd
        String parameters = String.format("nonce=%d&currencypair=%s&depth=%d&hours=%d&start_server_time=%d&exchangesite=%s",
                System.currentTimeMillis(), currencyPair, depthSelection, hoursSelection, start_server_time, ExchangeSite);

        String Result = post("https://bitcoinbot.azure-mobile.net/api/btce_select_graph_data?", parameters, "");
        if (Result == null) {
            return false;
        }
        JSONParser parser = new JSONParser();
        try {
            JSONArray arrays = (JSONArray) parser.parse(Result);
            for (Object obj_ : arrays) {
                JSONObject obj = (JSONObject) obj_;

                TickerItemData item = new TickerItemData(obj);

                list_BTCe2.add(item);
            }
        } catch (ParseException exp) {
            exp.printStackTrace();
        }
        return true;
    }

    private static String post(String URL, String parameters, String PostStr) {
        BufferedReader in = null;
        try {
            URL obj = new URL(URL + parameters);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", Constants.Server_UserAgent);
            con.setRequestProperty("Auth", Constants.Server_UserAgent);
            con.setRequestProperty("Server", Constants.Server_UserAgentAzure);
            con.setRequestProperty("X-ZUMO-APPLICATION", Constants.Azure_X_ZUMO_APPLICATION);

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(PostStr);
                wr.flush();
            }
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
        }
        return null;
    }
}
