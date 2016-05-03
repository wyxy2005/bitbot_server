package _client.apis;


/*import org.json.JSONArray;
import org.json.JSONObject;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;

public class _FybSGSE  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        // btc_usd
        String[] symbol2 = symbol.split("_");
        boolean IsFybSG = symbol.contains("sgd");

        String FetchURL = String.format("https://www.fyb%s.%s/api/%s/tickerdetailed.json",
                IsFybSG ? "sg" : "se",
                IsFybSG ? "com" : "se",
                symbol2[1].toUpperCase());

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject reader = new JSONObject(ret);

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    reader.getDouble("ask"),
                    reader.getDouble("bid"),
                    reader.getDouble("vol") * reader.getDouble("bid"),
                    reader.getDouble("vol"), // volcur
                    reader.getDouble("last"),
                    reader.getDouble("bid"),
                    reader.getDouble("ask"),
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        // btc_usd
        String[] symbol2 = symbol.split("_");
        boolean IsFybSG = symbol.contains("sgd");

        String FetchURL = String.format("https://www.fyb%s.%s/api/%s/orderbook.json",
                IsFybSG ? "sg" : "se",
                IsFybSG ? "com" : "se",
                symbol2[1].toUpperCase());

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

    public ReturnData_Trades getTradesReturnData(String ticker, String pairNameLeft, String pairNameRight, long since, int firstTradeId) {
        // btc_usd
        String[] symbol = ticker.split("_");
        boolean IsFybSG = ticker.contains("sgd");

        final String FetchURL = String.format("https://www.fyb%s.%s/api/%s/trades.json%s",
                IsFybSG ? "sg" : "se",
                IsFybSG ? "com" : "se",
                symbol[1].toUpperCase(),
                firstTradeId != 0 ? "?since=" + firstTradeId : "");

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Trades trades = new ReturnData_Trades();
        try {
            JSONArray tradesArray = new JSONArray(ret);

            for (int i = tradesArray.length() - 1; i > 0; i--) {
                JSONObject obj = tradesArray.getJSONObject(i);

                double amount = obj.getDouble("amount");
                double price = obj.getDouble("price");
                long time = obj.getLong("date") * 1000; // Epoch time
                int tradeid = obj.getInt("tid");

                final TradesType tradeType = TradesType.NA;

                trades.TotalBuy += amount;
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