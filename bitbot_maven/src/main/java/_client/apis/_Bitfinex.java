package _client.apis;

/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;


public class _Bitfinex  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        symbol = symbol.replace("_", "");
        final String FetchURL = String.format("https://api.bitfinex.com/v1/ticker/%s", symbol); // {"mid":"636.47","bid":"636.05","ask":"636.89","last_price":"637.2","timestamp":"1392718125.869626748"}
        final String FetchURL_Today = String.format("https://api.bitfinex.com/v1/today/%s", symbol); // {"low":"620.11","high":"664.0","volume":"13729.18712241"}

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        final String ret2 = HttpClientWrapper.queryHttpHttps(FetchURL_Today);
        if (ret == null)
            return null;

        try {
            JSONObject jsonObject1 = new JSONObject(ret);
            JSONObject jsonObject2 = new JSONObject(ret2);

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    jsonObject2.getDouble("high"),
                    jsonObject2.getDouble("low"),
                    jsonObject2.getDouble("volume") * jsonObject1.getDouble("bid"),
                    jsonObject2.getDouble("volume"), // volcur
                    jsonObject1.getDouble("last_price"),
                    jsonObject1.getDouble("bid"),
                    jsonObject1.getDouble("ask"),
                    System.currentTimeMillis());
            retData.setAvg(jsonObject1.getDouble("mid"));

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        symbol = symbol.replace("_", "");
        final String FetchURL = String.format("https://api.bitfinex.com/v1/book/%s", symbol);

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
        symbol = symbol.replace("_", "");
        final String FetchURL = String.format("https://api.bitfinex.com/v1/trades/%s?timestamp=%d", symbol, since / 1000);

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
                int tradeid = 0;

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