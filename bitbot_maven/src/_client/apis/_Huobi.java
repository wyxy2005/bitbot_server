package _client.apis;

/*import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import bitcoin.bitbot.ViewModelItem.TradesType;
import bitcoin.bitbot.ViewModelItem.TradesViewModelItem;
import bitcoin.bitbot.charts.MarketDepth;
import bitcoin.bitbot.etc.Constants;
import bitcoin.bitbot.etc.HttpClientWrapper;


public class _Huobi  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        String[] split = symbol.split("_");
        final String FetchURL = String.format("http://api.huobi.com/staticmarket/ticker_%s_json.js", split[0]);

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject reader = new JSONObject(ret);
            JSONObject ticker = reader.getJSONObject("ticker");

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    ticker.getDouble("high"),
                    ticker.getDouble("low"),
                    ticker.getDouble("vol") * ticker.getDouble("buy"),
                    ticker.getDouble("vol"), // volcur
                    ticker.getDouble("last"),
                    ticker.getDouble("open"), // buy
                    ticker.getDouble("sell"),
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        String[] split = symbol.split("_");
        String FetchURL = String.format("http://api.huobi.com/staticmarket/depth_%s_json.js ", split[0]);

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
        String[] split = symbol.split("_");
        String FetchURL = String.format("http://api.huobi.com/staticmarket/detail_%s_json.js ", split[0]);

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        final ReturnData_Trades trades = new ReturnData_Trades();
        try {
            JSONObject jsonObject = new JSONObject(ret);
            JSONArray tradesArray = jsonObject.getJSONArray("trades");

            for (int i = 0; i < tradesArray.length(); i++) {
                JSONObject obj = tradesArray.getJSONObject(i);

                String[] time = obj.getString("time").split(":");

                Calendar calendar = new GregorianCalendar();
                calendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                calendar.set(Calendar.SECOND, Integer.parseInt(time[2]));

                double amount = obj.getDouble("amount");
                double price = obj.getDouble("price");
                boolean isBuy = obj.getString("en_type").equals("bid");
                int tradeid = 0;

                final TradesType tradeType = isBuy ? TradesType.Buy : TradesType.Sell;

                if (isBuy)
                    trades.TotalBuy += amount;
                else
                    trades.TotalSell += amount;

                final TradesViewModelItem item = new TradesViewModelItem(calendar.getTimeInMillis(), price, amount, tradeid, tradeType);
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