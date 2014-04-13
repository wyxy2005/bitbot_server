package bitbot.graph;

import bitbot.cache.tickers.TickerItem;
import java.util.ArrayList;

/**
 *
 * @author z
 * Credits: http://www.iexplain.org/ema-how-to-calculate/
 * http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
 * https://code.google.com/p/adaptive-trading-system/source/browse/ATS/src/ats/strategies/EMA.java
 */
public class ExponentialMovingAverage {

    public static ArrayList<ExponentialMovingAverageData> CalculateEMA(final ArrayList<TickerItem> records, int EMA, long startTime) {
        ArrayList<ExponentialMovingAverageData> ret = new ArrayList<>();
        double yesterdayEMA = -1;
        
        for (TickerItem sdr : records) {
            if (yesterdayEMA == -1) // default value
                yesterdayEMA = sdr.getOpen();
            
            //call the EMA calculation
            double ema = CalculateEMAInternal(sdr.getOpen(), EMA, yesterdayEMA);
            
            //put the calculated ema in an array
            if (startTime < sdr.getServerTime())
                ret.add(new ExponentialMovingAverageData(sdr.getServerTime(), ema, sdr.getOpen()));
            
            //make sure yesterdayEMA gets filled with the EMA we used this time around
            yesterdayEMA = ema;
        }
        return ret;
    }

    private static double CalculateEMAInternal(double priceNow, int numberOfDays, double priceEMAYesterday) {
        double k = 2 / (numberOfDays + 1d);

        return priceNow * k + priceEMAYesterday * (1d - k);
    }
}
