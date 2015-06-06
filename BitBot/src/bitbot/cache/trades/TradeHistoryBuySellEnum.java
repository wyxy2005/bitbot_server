package bitbot.cache.trades;

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
    
    public static TradeHistoryBuySellEnum getEnumByValue(byte value) {
        for (TradeHistoryBuySellEnum type : values()) {
            if (type.getValue() == value){
                return type;
            }
        }
        return TradeHistoryBuySellEnum.Unknown;
    }
}
