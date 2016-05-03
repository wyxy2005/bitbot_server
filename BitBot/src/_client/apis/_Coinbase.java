package _client.apis;

/*import org.json.JSONObject;

import bitcoin.bitbot.etc.HttpClientWrapper;

public class _Coinbase  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        final String FetchURL = "https://coinbase.com/api/v1/prices/buy";

        final String ret = HttpClientWrapper.queryHttpHttps(FetchURL);
        if (ret == null)
            return null;

        try {
            JSONObject retJsonObj = new JSONObject(ret);

            double amount = retJsonObj.getDouble("amount");
            double subTotalAmount = retJsonObj.getJSONObject("subtotal").getDouble("amount");

            ReturnData_Ticker retData = new ReturnData_Ticker(
                    subTotalAmount,
                    subTotalAmount,
                    0, // vol
                    0, // volcur
                    amount,
                    amount, // buy
                    amount,
                    System.currentTimeMillis());

            return retData;
        } catch (Exception exp) {
            Log.e("bitbot", exp.toString());
        }
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        return null;
    }

    public ReturnData_Trades getTradesReturnData(String symbol, String pairNameLeft, String pairNameRight, long since, int firstTradeId) {
        return null;
    }
}*/