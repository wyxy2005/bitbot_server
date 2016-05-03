package _client.apis;

public class _MtGox  implements ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol) {
        return null;
    }

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice) {
        return null;
    }

    public ReturnData_Trades getTradesReturnData(String symbol, String pairNameLeft, String pairNameRight, long since, int firstTradeId) {
        return null;
    }
}