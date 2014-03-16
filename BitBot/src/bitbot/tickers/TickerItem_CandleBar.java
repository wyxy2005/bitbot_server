package bitbot.tickers;

/**
 *
 * @author z
 */
public class TickerItem_CandleBar {

    private final long server_time;
    private final float close, high, low, open;

    public TickerItem_CandleBar(long _server_time, float _close, float _high, float _low, float _open) {
        server_time = _server_time;
        close = _close;
        high = _high;
        low = _low;
        open = _open;
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
}
