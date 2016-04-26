package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class TickerHistory_ItBit implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_ItBit(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }
    
    
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); // 2015-12-17T17:33:40.510317Z

    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        String Uri = String.format("https://api.itbit.com/v1/markets/%s/trades?since=%d", CurrencyPair.replace("_", "").toUpperCase(), LastTradeId);
        String GetResult = HttpClient.httpsGet(Uri, "");

        if (GetResult != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(_TickerCacheTaskSource, LastPurchaseTime, LastTradeId, 0, false);

            JSONParser parser = new JSONParser(); // Init parser
            try {
                // Timestamp for last purchase time
                //Calendar cal_LastPurchaseTime = Calendar.getInstance();

                // Container factory for the JSON array to persist the order
                ContainerFactory containerFactory = new ContainerFactory() {
                    @Override
                    public List creatArrayContainer() {
                        return new LinkedList();
                    }

                    @Override
                    public Map createObjectContainer() {
                        return new LinkedHashMap();
                    }
                };
                LinkedHashMap data = (LinkedHashMap) parser.parse(GetResult, containerFactory);
                LinkedList<LinkedHashMap> recenttrades = (LinkedList<LinkedHashMap>) data.get("recentTrades");

                for (LinkedHashMap obj : recenttrades) {
                    float amount = Float.parseFloat(obj.get("amount").toString());
                    float price = Float.parseFloat(obj.get("price").toString());
                    String strdate = obj.get("timestamp").toString();
                    TradeHistoryBuySellEnum type = TradeHistoryBuySellEnum.Unknown;
                    long tradeid = Long.parseLong(obj.get("matchNumber").toString());


                    Date dateobj = df.parse(strdate); // 2016-04-26T13:19:36.3030000Z
                    long date = dateobj.getTime();
                    
                    // Initialize last purchase time if neccessary
                    if (LastPurchaseTime == 0) {
                        LastPurchaseTime = date - 1; // set default param
                        /*cal_LastPurchaseTime = Calendar.getInstance();
                         cal_LastPurchaseTime.set(Calendar.YEAR, 1970);
                         cal_LastPurchaseTime.set(Calendar.MONTH, 0);
                         cal_LastPurchaseTime.set(Calendar.DATE, 0);
                        
                         cal_LastPurchaseTime.add(Calendar.HOUR, -4); // BTC-e, time 
                         cal_LastPurchaseTime.add(Calendar.SECOND, (int) (date / 1000));*/

                        ReturnData.setLastPurchaseTime(LastPurchaseTime);
                    }

                    //http://tutorials.jenkov.com/java-date-time/java-util-timezone.html
                    // Timestamp for trades
                   /* Calendar cal = Calendar.getInstance(); // BTCe time
                     cal.set(Calendar.YEAR, 1970);
                     cal.set(Calendar.MONTH, 0);
                     cal.set(Calendar.DATE, 0);
                    
                     cal.add(Calendar.HOUR, -4); // BTC-e, time 
                     cal.add(Calendar.SECOND, (int) (date / 1000));
                    
                     System.out.println(String.format("[Trades history] Got [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));*/
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", date, price, amount));
                        ReturnData.merge(price, amount, date, tradeid, type);

                        if (enableTrackTrades && 
                                LastTradeId != 0) { // Ensure that its not during server start-up.
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeSite, CurrencyPair);
                        }
                    }
                    if (tradeid > ReturnData.getLastTradeId()) {
                        ReturnData.setLastTradeId(tradeid);
                    }
                }
            } catch (Exception parseExp) {
                System.out.println(ExchangeSite + " " + CurrencyPair);
                parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
            return ReturnData;
        }
        return null;
    }
}
