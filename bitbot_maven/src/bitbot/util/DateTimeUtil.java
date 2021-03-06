package bitbot.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author z
 */
public class DateTimeUtil {

    private static final long TICKS_AT_EPOCH = 621355968000000000L;
    private static final long TICKS_PER_MILLISECOND = 10000;
    
    //                                                                  2013-12-29T02:22:47.660Z
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.'Z'");

    public static long DateTimeNowTicks_CSharp() {
        /*     long ticks = 634200192000000000L;

         Date date = new Date((ticks - TICKS_AT_EPOCH) / TICKS_PER_MILLISECOND);

         TimeZone utc = TimeZone.getTimeZone("UTC");
         Calendar calendar = Calendar.getInstance(utc);
         calendar.setTime(date);
        
         return calendar*/
        return 0;
    }

    public static Date convertMSSQLDateTime(String s) {
        System.out.println(s);
        try {
            Date d = sdf.parse(s);
            
            return d;
        } catch (ParseException exp) {
            exp.printStackTrace();
        }
        return null;
    }
    
    public static Date convertDateTime(long s) {
        return new Date(s * 1000);
    }
    
    
    /// Rounding time
    public static long roundToCandlestickOpening(long ServerTimeFrom, int intervalMinutes) {
        final long cTime_Millis = System.currentTimeMillis();
        
        if (ServerTimeFrom == 0)
            return 0;
        
        final Calendar dtCal = Calendar.getInstance();
        dtCal.setTimeInMillis(ServerTimeFrom * 1000); // set UTC time
        
        if (intervalMinutes < 60) { // go by the hour
            
        } else if (intervalMinutes < 60 * 24) { // go by the daily
            
        } else if (intervalMinutes < 60 * 24 * 30) { // go by the month
            
        }
        
        return 0;
    }
}
