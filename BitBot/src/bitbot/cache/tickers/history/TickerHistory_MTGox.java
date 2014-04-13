package bitbot.cache.tickers.history;

/**
 *
 * @author z
 */
public class TickerHistory_MTGox implements TickerHistory {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime) {
        return null;
    }
}
