package bitbot.cache.tickers;

/**
 *
 * @author z
 */
public interface TickerHistoryInterface {
    
    public boolean enableTrackTrades();
    
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId);
}
