package _client.apis;

public interface ExchangeAPIInterface {

    public ReturnData_Ticker getTickerReturnData(String symbol);

    public ReturnData_Depth getDepthReturnData(String symbol, double currentBuyPrice, double currentSellPrice);

    public ReturnData_Trades getTradesReturnData(String symbol, String pairNameLeft, String pairNameRight, long since, int firstTradeId);

}
