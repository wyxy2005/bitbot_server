package bitbot.cache.swaps;

/**
 *
 * @author z
 */
public interface SwapsInterface {
    
    public SwapsHistoryData connectAndParseSwapsResult(String ExchangeSite, String Currency, String ExchangeCurrency);
}
