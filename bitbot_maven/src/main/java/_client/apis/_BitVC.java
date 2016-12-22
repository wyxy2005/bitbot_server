package _client.apis;


/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;

public class _BitVC implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        final String FetchURL;

        if (symbol.contains("Quarterly")) {
            FetchURL = "http://market.bitvc.com/futures/ticker_btc_quarter.js";
        } else if (symbol.contains("BiWeekly")) {
            FetchURL = "http://market.bitvc.com/futures/ticker_btc_next_week.js";
        } else {
            FetchURL = "http://market.bitvc.com/futures/ticker_btc_week.js";
        }

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject jsonObj = new JSONObject(ret);

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    jsonObj.getDouble("high"),
                    jsonObj.getDouble("low"),
                    jsonObj.getDouble("vol") * jsonObj.getDouble("buy"),
                    jsonObj.getDouble("vol"), // volcur
                    jsonObj.getDouble("last"),
                    jsonObj.getDouble("buy"),
                    jsonObj.getDouble("sell"),
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        String FetchURL  = null;
        if (symbol.contains("Quarterly")) {
            FetchURL = "http://market.bitvc.com/futures/depths_btc_quarter.js";
        } else if (symbol.contains("BiWeekly")) {
            FetchURL = "http://market.bitvc.com/futures/depths_btc_next_week.js";
        } else {
            FetchURL = "http://market.bitvc.com/futures/depths_btc_week.js";
        }

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
                    JSONArray itemArray = (JSONArray) asksArray.get(i);

                    double price = itemArray.getDouble(0);
                    double amount = itemArray.getDouble(1);

                    if (currentBuyPrice != 0 && price > CurrentBuyPrice_Low && price < CurrentBuyPrice_High) {
                        depth.asks.add(new MarketDepth(false, price, amount, 0));

                        depth.UpdateMaxMinCostVolumeData(amount, price, false);
                    }
                }
            }
            if (bidsArray != null) {
                for (int i = 0; i < bidsArray.length(); ++i) {
                    JSONArray itemArray = (JSONArray) bidsArray.get(i);

                    double price = itemArray.getDouble(0);
                    double amount = itemArray.getDouble(1);

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
        String FetchURL  = null;
        if (symbol.contains("Quarterly")) {
            FetchURL = "http://market.bitvc.com/futures/trades_btc_quarter.js";
        } else if (symbol.contains("BiWeekly")) {
            FetchURL = "http://market.bitvc.com/futures/trades_btc_next_week.js";
        } else {
            FetchURL = "http://market.bitvc.com/futures/trades_btc_week.js";
        }

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
                long time = obj.getLong("date") * 1000; // Epoch time
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
}
*/