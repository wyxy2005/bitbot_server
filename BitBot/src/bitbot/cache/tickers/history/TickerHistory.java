package bitbot.cache.tickers.history;

/**
 *
 * @author z
 */
public interface TickerHistory {
    
    public TickerHistoryData connectAndParseHistoryResult(String CurrencyPair, long LastPurchaseTime);
}
