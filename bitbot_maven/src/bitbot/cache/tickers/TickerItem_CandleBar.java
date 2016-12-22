package bitbot.cache.tickers;

/**
 *
 * @author z
 */
public class TickerItem_CandleBar {

    private final long server_time;
    private final float close, high, low, open, buysell_ratio;
    private final double volume, volume_cur;
    private final boolean isUnmaturedData;

    public TickerItem_CandleBar(long _server_time, float _close, float _high, float _low, float _open, double _volume, double _volume_cur, float _buysell_ratio, boolean _isUnmaturedData) {
        server_time = _server_time;
        close = _close;
        high = _high;
        low = _low;
        open = _open;
        volume = _volume;
        volume_cur = _volume_cur;
        buysell_ratio = _buysell_ratio;
        isUnmaturedData = _isUnmaturedData;
    }

    public long getServerTime() {
        return server_time;
    }

    public float getClose() {
        return close;
    }

    public float getHigh() {
        return high;
    }

    public float getLow() {
        return low;
    }

    public float getOpen() {
        return open;
    }
    
    public double getVol() {
        return volume;
    }

    public double getVol_Cur() {
        return volume_cur;
    }
    
    public float getBuySell_Ratio() {
        return buysell_ratio;
    }
    
    public boolean getIsUnmaturedData() {
        return isUnmaturedData;
    }
}
