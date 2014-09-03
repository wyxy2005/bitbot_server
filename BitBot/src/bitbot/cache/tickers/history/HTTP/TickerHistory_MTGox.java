package bitbot.cache.tickers.history.HTTP;

import bitbot.cache.tickers.history.TickerHistory;
import bitbot.cache.tickers.history.TickerHistoryData;
import bitbot.server.Constants;

/**
 *
 * @author z
 */
public class TickerHistory_MTGox implements TickerHistory {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");
    
    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        return null;
    }
}
