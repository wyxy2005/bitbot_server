package bitbot.graph;

/**
 *
 * @author z
 */
public class ExponentialMovingAverageData {
    private final long ServerTime;
    private final double EMA;
    private final double price;
    
    public ExponentialMovingAverageData(long _ServerTime, double _EMA, double _price) {
        ServerTime = _ServerTime;
        EMA = _EMA;
        price = _price;
    }
    
    public long getServerTime() {
        return ServerTime;
    }
    
    public double getEMA() {
        return EMA;
    }
    
    public double getPrice() {
        return price;
    }
}
