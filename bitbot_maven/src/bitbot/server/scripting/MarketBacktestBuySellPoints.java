package bitbot.server.scripting;

import bitbot.cache.tickers.TickerItem_CandleBar;

/**
 *
 * @author z
 */
public class MarketBacktestBuySellPoints {
    
    private final TickerItem_CandleBar tickerItem_Candlebar;
    private final float amount, price;
    private boolean isBuy = false;
    
    public MarketBacktestBuySellPoints(TickerItem_CandleBar _tickerItem_Candlebar, float _amount, float _price, boolean _isBuy) {
        this.tickerItem_Candlebar = _tickerItem_Candlebar;
        this.amount = _amount;
        this.price = _price;
        this.isBuy = _isBuy;
    }
    
    public TickerItem_CandleBar getTickerItem_Candlebar() {
        return tickerItem_Candlebar;
    }
    
    public float getAmount() {
        return amount;
    }
    
    public float getPrice() {
        return price;
    }
    
    public boolean isBuy() {
        return isBuy;
    }
}
