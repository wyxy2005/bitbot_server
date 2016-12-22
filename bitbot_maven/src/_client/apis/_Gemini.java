package _client.apis;


/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;

public class _Gemini  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        String[] split = symbol.toUpperCase().split("_");
        final String FetchURL = String.format("https://api.gemini.com/v1/book/%s%s",
                split[0],
                split[1]);

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject reader = new JSONObject(ret);
            JSONArray bidJsonObj = reader.getJSONArray("bids");
            JSONArray askJsonObj = reader.getJSONArray("asks");

            JSONObject highestBidObj = (JSONObject) bidJsonObj.get(0);
            JSONObject lowestAskObj = (JSONObject) askJsonObj.get(0);

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    lowestAskObj.getDouble("price"),
                    highestBidObj.getDouble("price"),
                    0,
                    0, // volcur
                    highestBidObj.getDouble("price"),
                    highestBidObj.getDouble("price"),
                    lowestAskObj.getDouble("price"),
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        String[] split = symbol.toUpperCase().split("_");
        final String FetchURL = String.format("https://api.gemini.com/v1/book/%s%s",
                split[0],
                split[1]);

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Depth depth = new ReturnData_Depth();

        double CurrentBuyPrice_Low = currentBuyPrice * (1d - Constants.DepthPercentageRange);
        double CurrentBuyPrice_High = currentSellPrice * (1d + Constants.DepthPercentageRange);

        try {
            JSONObject reader = new JSONObject(ret);
            JSONArray asksArray = reader.getJSONArray("asks");
            JSONArray bidsArray = reader.getJSONArray("bids");

            if (asksArray != null) {
                for (int i = 0; i < asksArray.length(); ++i) {
                    JSONObject item = (JSONObject) asksArray.get(i);

                    double price = item.getDouble("price");
                    double amount = item.getDouble("amount");

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
                    double amount = item.getDouble("amount");

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
        String[] split = symbol.toUpperCase().split("_");
        final String FetchURL = String.format("https://api.gemini.com/v1/trades/%s%s%s",
                split[0],
                split[1],
                since > 1 ? ("?since=" + since + 1) : "");

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Trades trades = new ReturnData_Trades();
        try {
            JSONArray tradesArray = new JSONArray(ret);

            for (int i = 0; i < tradesArray.length(); i++) {
                JSONObject obj = tradesArray.getJSONObject(i);

                double amount = obj.getDouble("amount");
                double price = obj.getDouble("price");
                long time = obj.getLong("timestamp") * 1000; // Epoch time
                boolean isBuy = obj.getString("type").equals("buy");
                int tradeid = obj.getInt("tid");

                final TradesType tradeType = isBuy ? TradesType.Buy : TradesType.Sell;

                if (isBuy)
                    trades.TotalBuy += amount;
                else
                    trades.TotalSell += amount;

                final TradesViewModelItem item = new TradesViewModelItem(time, price, amount, tradeid, tradeType);
                trades.tradesList.add(item);

                if (i > Constants.TradesDisplayLimit)
                    break; // limit
            }
            return trades;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }
}*/