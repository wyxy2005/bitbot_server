package bitbot.cache.tickers.index;

/**
 *
 * @author twili
 */
public enum TickerHistoryIndexCurrency {
    ChineseYuan("cny"),
    USDollar("usd"),
    JapaneseYen("jpy"),;
    private final String currency;

    private TickerHistoryIndexCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }
}
