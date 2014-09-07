package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.server.Constants;

/**
 *
 * @author z
 */
public class TickerHistory_MTGox implements TickerHistoryInterface {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");
    
    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        return null;
    }
}
