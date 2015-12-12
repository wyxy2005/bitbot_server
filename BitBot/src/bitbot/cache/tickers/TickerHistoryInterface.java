package bitbot.cache.tickers;

import bitbot.cache.tickers.TickerCacheTask.TickerCacheTask_ExchangeHistory;

/**
 *
 * @author z
 */
public interface TickerHistoryInterface {
    
    public boolean enableTrackTrades();
    
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask_ExchangeHistory _TickerCacheTaskSource,
            String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId);
}
