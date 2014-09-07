package bitbot.cache.tickers;

/**
 *
 * @author z
 */
public interface TickerHistoryInterface {
    
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId);
}
