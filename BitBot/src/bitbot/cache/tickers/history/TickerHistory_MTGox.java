package bitbot.cache.tickers.history;

import bitbot.server.Constants;

/**
 *
 * @author z
 */
public class TickerHistory_MTGox implements TickerHistory {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");

    private long lastBroadcastedTime = 0;

    private boolean readyToBroadcastPriceChanges() {
        final long cTime = System.currentTimeMillis();
        if (cTime - lastBroadcastedTime > Constants.PriceBetweenServerBroadcastDelay) {
            lastBroadcastedTime = cTime;
            return true;
        }
        return false;
    }
    
    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        return null;
    }
}
