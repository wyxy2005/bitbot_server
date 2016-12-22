package _client.apis;

/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;

public class _Cryptsy  implements ExchangeAPIInterface {

    public static int GetMarketId(String pair)
    {
        switch (pair)
        {
            case "ltc_usd":
                return 1;
            case "btc_usd":
                return 2;
            case "ltc_btc":
                return 3;
            case "ftc_usd":
                return 6;
            case "doge_btc":
                return 132;
            case "drk_btc":
                return 155;
            case "nxt_btc":
                return 159;
            case "rdd_btc":
                return 169;
            case "bc_btc":
                return 179;
            case "doge_usd":
                return 182;
            case "drk_usd":
                return 213;
            case "uro_btc":
                return 247;
            case "btcd_btc":
                return 256;
            case "rdd_usd":
                return 262;
            case "cann_btc":
                return 300;
        }
        return -1;
    }

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        final String FetchURL = String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%s", GetMarketId(symbol));

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject reader = new JSONObject(ret);
            if (reader.getInt("success") == 1) {
                JSONObject jsonReturnObj = reader.getJSONObject("return");

                String[] pairSplit = symbol.split("_");
                JSONObject jsonMarketObj = jsonReturnObj.getJSONObject("markets");
                JSONObject jsonPairMarketObj = jsonMarketObj.getJSONObject(pairSplit[0].toUpperCase());

                ReturnData_Ticker retData = new ReturnData_Ticker(
                        jsonPairMarketObj.getDouble("lasttradeprice"),
                        jsonPairMarketObj.getDouble("lasttradeprice"),
                        jsonPairMarketObj.getDouble("volume") * jsonPairMarketObj.getDouble("lasttradeprice"),
                        jsonPairMarketObj.getDouble("volume"), // volcur
                        jsonPairMarketObj.getDouble("lasttradeprice"),
                        jsonPairMarketObj.getDouble("lasttradeprice"),
                        jsonPairMarketObj.getDouble("lasttradeprice"),
                        System.currentTimeMillis());

                return retData;
            }
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        final String FetchURL = String.format("http://pubapi.cryptsy.com/api.php?method=singleorderdata&marketid=%s", GetMarketId(symbol));

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Depth depth = new ReturnData_Depth();
        String[] pairSplit = symbol.split("_");

        double CurrentBuyPrice_Low = currentBuyPrice * (1d - Constants.DepthPercentageRange);
        double CurrentBuyPrice_High = currentSellPrice * (1d + Constants.DepthPercentageRange);

        try {
            JSONObject reader = new JSONObject(ret);
            JSONObject pairMarket =   reader.getJSONObject(pairSplit[0].toUpperCase());
            JSONArray asksArray = pairMarket.getJSONArray("sellorders");
            JSONArray bidsArray = pairMarket.getJSONArray("buyorders");

            if (asksArray != null) {
                for (int i = 0; i < asksArray.length(); ++i) {
                    JSONObject item = (JSONObject) asksArray.get(i);

                    double price = item.getDouble("price");
                    double amount = item.getDouble("quantity");

                    if (currentBuyPrice != 0 && price > CurrentBuyPrice_Low && price < CurrentBuyPrice_High) {
                        depth.asks.add(new MarketDepth(false, price, amount, 0));

                        depth.UpdateMaxMinCostVolumeData(amount, price, false);
                    }
                }
            }
            if (bidsArray != null) {
                for (int i = 0; i < bidsArray.length(); ++i) {
                    JSONObject item = (JSONObject) bidsArray.get(i);

                    double price = item.getDouble("price");
                    double amount = item.getDouble("quantity");

                    if (currentBuyPrice != 0 && price > CurrentBuyPrice_Low && price < CurrentBuyPrice_High) {
                        depth.bids.add(new MarketDepth(true, price, amount, 0));

                        depth.UpdateMaxMinCostVolumeData(amount, price, true);
                    }
                }
            }
            // Process data
            depth.ProcessData();

            return depth;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Trades getTradesReturnData(String symbol, String pairNameLeft, String pairNameRight, long since, int firstTradeId) {
        return null;
    }
}*/