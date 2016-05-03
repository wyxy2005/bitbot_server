package bitbot.util.database;

import bitbot.cache.swaps.SwapsItemData;
import bitbot.Constants;
import bitbot.util.database.DatabaseConnection;
import bitbot.cache.tickers.TickerItemData;
import bitbot.cache.trades.TradesItemData;
import bitbot.util.database.DatabaseTablesConstants;
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
import java.util.List;
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
public class MicrosoftAzureDatabaseExt {

    /*   
    * Returns the graph data selected from the MSSQL Datababase
    * @return long (biggest server time), -2 if error, -1 if no result
     */
    public static long selectGraphData(String ExchangeSite, String currencyPair, int depthSelection, long start_server_time, List<TickerItemData> list_BTCe2) {
        // currencyPair = eg: btc_usd
        String tableName = DatabaseTablesConstants.getDatabaseTableName(ExchangeSite, currencyPair);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            //ps = con.prepareStatement("SELECT TOP " + depthSelection + "  \"high\", \"low\", \"open\", \"close\", \"vol\", \"vol_cur\", \"server_time\", \"buysell_ratio\" FROM BitCoinBot." + tableName + " WHERE server_time > ? AND __createdAt < dateadd(hh, + " + hoursSelection + ", getdate()) ORDER BY server_time ASC;");
            ps = con.prepareStatement("SELECT TOP " + depthSelection + "  \"high\", \"low\", \"open\", \"close\", \"vol\", \"vol_cur\", \"server_time\", \"buysell_ratio\" FROM BitCoinBot." + tableName + " WHERE server_time > ? ORDER BY server_time ASC;");
            ps.setLong(1, start_server_time);

            rs = ps.executeQuery();

            long biggest_ServerTime = -1;
            if (rs != null) {
                while (rs.next()) {
                    TickerItemData item = new TickerItemData(rs);

                    list_BTCe2.add(item);

                    if (item.getServerTime() > biggest_ServerTime) {
                        biggest_ServerTime = item.getServerTime();
                    }
                }
                return biggest_ServerTime;
            }
            return -2;
        } catch (Exception e) { // catch exception instead of SQLException. Might throw SQLServerException.
            e.printStackTrace();
            return -2;
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
    }

    /*   
    * Returns the swaps data selected from the MSSQL Datababase
    * @return long (biggest server time), -1 if error
     */
    public static long selectSwapsData(String ExchangeSite, String currency, int depthSelection, long start_server_time, List<SwapsItemData> list_items) {
        final String tableName = String.format("%s_swaps_%s", ExchangeSite, currency);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            //ps = con.prepareStatement("SELECT TOP " + depthSelection + "  \"rate\", \"amount_lent\", \"timestamp\", \"spot_price\" FROM BitCoinBot." + tableName + " WHERE timestamp > ? AND __createdAt < dateadd(hh, + " + hoursSelection + ", getdate()) ORDER BY timestamp ASC;");
            ps = con.prepareStatement("SELECT TOP " + depthSelection + "  \"rate\", \"amount_lent\", \"timestamp\", \"spot_price\" FROM BitCoinBot." + tableName + " WHERE timestamp > ? ORDER BY timestamp ASC;");
            ps.setLong(1, start_server_time);

            rs = ps.executeQuery();

            long biggest_ServerTime = 0;
            if (rs != null) {
                while (rs.next()) {
                    SwapsItemData item = new SwapsItemData(rs);

                    list_items.add(item);

                    if (item.getTimestamp() > biggest_ServerTime) {
                        biggest_ServerTime = item.getTimestamp();
                    }
                }
                return biggest_ServerTime;
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
        return -1;
    }

    public static long selectTradesData(String ExchangeSite, String currency, int depthSelection, long start_server_time, List<TradesItemData> list_items) {
        final String tableName = DatabaseTablesConstants.getDatabaseTableName_Trades(ExchangeSite, currency);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement("SELECT TOP " + depthSelection + "  \"price\", \"amount\", \"LastPurchaseTime\", \"type\" FROM BitCoinBot." + tableName + " WHERE LastPurchaseTime > ? ORDER BY LastPurchaseTime ASC;");
            ps.setLong(1, start_server_time);

            rs = ps.executeQuery();

            long biggest_ServerTime = 0;
            if (rs != null) {
                while (rs.next()) {
                    TradesItemData item = new TradesItemData(rs);

                    list_items.add(item);

                    if (item.getLastPurchaseTime() > biggest_ServerTime) {
                        biggest_ServerTime = item.getLastPurchaseTime();
                    }
                }
                return biggest_ServerTime;
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
        return -1;
    }

    @Deprecated
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

    public static String[] selectShowSummaryCurrencyPairs() {
        final String Result = get("https://bitcoinbot.azure-mobile.net/api/list_currencies?", "");
        if (Result != null) {

            final JSONParser parser = new JSONParser();
            try {
                final JSONObject jsonObj = (JSONObject) parser.parse(Result);

                final String retmsg = jsonObj.get("message").toString();

                return retmsg.split("---");
            } catch (ParseException exp) {
                exp.printStackTrace();
            }
        }
        // gonna return something regardless.
        return "gemini-btc_usd---btce-btc_usd---btce-btc_eur---btce-btc_rur---btce-ltc_usd---btce-ltc_btc---btce-ltc_eur---btce-ltc_rur---btce-nmc_usd---btce-nmc_btc---btce-usd_rur---btce-eur_usd---btce-nvc_usd---btce-nvc_btc---btce-ppc_usd---btce-ppc_btc---btce-eth_usd---btce-eth_btc---btce-dsh_btc---bitstamp-btc_usd---bitstamp-btc_eur---okcoin-btc_cny---okcoin-ltc_cny---okcoininternational-btc_usd---okcoininternational-ltc_usd---okcoininternational-btc Futures Weekly_usd---okcoininternational-btc Futures BiWeekly_usd---okcoininternational-btc Futures Quarterly_usd---okcoininternational-ltc Futures Weekly_usd---okcoininternational-ltc Futures BiWeekly_usd---okcoininternational-ltc Futures Quarterly_usd---huobi-btc_cny---huobi-ltc_cny---coinbase-btc_usd---coinbaseexchange-btc_usd---coinbaseexchange-btc_gbp---coinbaseexchange-btc_eur---coinbaseexchange-btc_cad---btcchina-btc_cny---btcchina-ltc_btc---btcchina-ltc_cny---campbx-btc_usd---itbit-xbt_usd---itbit-xbt_sgd---itbit-xbt_eur---bitfinex-btc_usd---bitfinex-ltc_usd---bitfinex-ltc_btc---bitfinex-eth_usd---bitfinex-eth_btc---kraken-xbt_usd---kraken-xbt_eur---kraken-eth_xbt---cexio-ghs_btc---fybsg-btc_sgd---fybse-btc_sek---_796-btc Futures_usd---bitvc-btc Futures Weekly_cny---bitvc-btc Futures Quarterly_cny---bitvc-btc Futures BiWeekly_cny"
                .split("---");
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

    private static String get(String URL, String parameters) {
        BufferedReader in = null;
        try {
            URL obj = new URL(URL + parameters);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add reuqest header
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", Constants.Server_UserAgent);
            con.setRequestProperty("Auth", Constants.Server_UserAgent);
            con.setRequestProperty("Server", Constants.Server_UserAgentAzure);
            con.setRequestProperty("X-ZUMO-APPLICATION", Constants.Azure_X_ZUMO_APPLICATION);

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
