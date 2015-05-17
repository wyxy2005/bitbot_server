package bitbot.cache.tickers;

/**
 *
 * @author z
 */
public enum TradeHistoryBuySellEnum {
    Buy(1),
    Sell(0),
    Unknown(2);
    
    int value;
    private TradeHistoryBuySellEnum(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
