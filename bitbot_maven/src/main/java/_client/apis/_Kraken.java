package _client.apis;


/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;

public class _Kraken  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        // symbol=ltc_cny
        String symbol2 = symbol.replace("_", "").toUpperCase();
        final String FetchURL = String.format("https://api.kraken.com/0/public/Ticker?pair=%s", symbol2); // XBTUSD

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            final boolean isEthereum = symbol.contains("eth");
            final String pairName2 = "X" + symbol.replace("_", isEthereum ? "X" : "Z").toUpperCase();

            JSONObject reader = new JSONObject(ret);
            JSONObject resultJsonObj = reader.getJSONObject("result");
            JSONObject pairJsonObj = resultJsonObj.getJSONObject(pairName2);

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    Double.parseDouble(pairJsonObj.getJSONArray("h").get(0).toString()),
                    Double.parseDouble(pairJsonObj.getJSONArray("l").get(0).toString()),
                    Double.parseDouble(pairJsonObj.getJSONArray("v").get(0).toString()) * Double.parseDouble(pairJsonObj.getJSONArray("b").get(0).toString()),
                    Double.parseDouble(pairJsonObj.getJSONArray("v").get(0).toString()), // volcur
                    Double.parseDouble(pairJsonObj.getJSONArray("c").get(0).toString()),
                    Double.parseDouble(pairJsonObj.getJSONArray("b").get(0).toString()),
                    Double.parseDouble(pairJsonObj.getJSONArray("a").get(0).toString()),
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        // symbol=ltc_cny
        String symbol2 = symbol.replace("_", "").toUpperCase();
        final String FetchURL = String.format("https://api.kraken.com/0/public/Depth?pair=%s", symbol2); // XBTUSD

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Depth depth = new ReturnData_Depth();

        boolean isEthereum = symbol.contains("eth");
        String pairName2 = "X" + symbol.replace("_", isEthereum ? "X" : "Z").toUpperCase();

        double CurrentBuyPrice_Low = currentBuyPrice * (1d - Constants.DepthPercentageRange);
        double CurrentBuyPrice_High = currentSellPrice * (1d + Constants.DepthPercentageRange);

        try {
            JSONObject reader = new JSONObject(ret);
            JSONObject resultObj = reader.getJSONObject("result");
            JSONObject pairResultObj = resultObj.getJSONObject(pairName2);

            JSONArray asksArray = pairResultObj.getJSONArray("asks");
            JSONArray bidsArray = pairResultObj.getJSONArray("bids");

            if (asksArray != null) {
                for (int i = asksArray.length() -1; i > 0; i--) {
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
        String symbol2 = symbol.replace("_", "").toUpperCase();
        final String FetchURL = String.format("https://api.kraken.com/0/public/Trades?pair=%s", symbol2); // XBTUSD

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        boolean isEthereum = symbol.contains("eth");
        String pairName2 = "X" + symbol.replace("_", isEthereum ? "X" : "Z").toUpperCase();

        final ReturnData_Trades trades = new ReturnData_Trades();
        try {
            JSONObject reader = new JSONObject(ret);
            JSONObject resultObj = reader.getJSONObject("result");
            JSONArray tradesArray = resultObj.getJSONArray(pairName2);

            for (int i = tradesArray.length() - 1; i > 0; i--) {
                JSONArray obj = tradesArray.getJSONArray(i);

                double amount = obj.getDouble(1);
                double price = obj.getDouble(0);
                long time = (long) (obj.getDouble(2) * 1000l); // Epoch time
                boolean isBuy = obj.getString(3).equals("b");
                int tradeid = 0;

                final TradesType tradeType = isBuy ? TradesType.Buy : TradesType.Sell;

                if (isBuy)
                    trades.TotalBuy += amount;
                else
                    trades.TotalSell += amount;

                final TradesViewModelItem item = new TradesViewModelItem(time, price, amount, tradeid, tradeType);
                trades.tradesList.add(item);

                if (trades.tradesList.size() - i > Constants.TradesDisplayLimit)
                    break; // limit
            }
            return trades;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }
}*/