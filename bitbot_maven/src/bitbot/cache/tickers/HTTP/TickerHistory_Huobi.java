package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Documentation:
 * http://translate.google.com/translate?act=url&depth=1&hl=en&ie=UTF8&prev=_t&rurl=translate.google.com&sl=auto&tl=en&u=http://www.huobi.com/help/index.php%3Fa%3Dmarket_help
 * https://www.huobi.com/help/index.php?a=market_help
 * 
 * @author z
 */
public class TickerHistory_Huobi implements TickerHistoryInterface {

    private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT-8");

    private final boolean enableTrackTrades;

    public TickerHistory_Huobi(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        final String[] Split = CurrencyPair.split("_");
        final String Uri = String.format("http://api.huobi.com/staticmarket/detail_%s_json.js", Split[0]);
        String GetResult = HttpClient.httpGet(Uri, "");

        if (GetResult != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(_TickerCacheTaskSource, LastPurchaseTime, LastTradeId, 0, false);

            JSONParser parser = new JSONParser(); // Init parser
            try {
                // Timestamp for last purchase time
                Calendar cal_LastPurchaseTime = Calendar.getInstance(timeZone); // Huobi time

                // Container factory for the JSON array to persist the order
                /*ContainerFactory containerFactory = new ContainerFactory() {
                 @Override
                 public List creatArrayContainer() {
                 return new LinkedList();
                 }

                 @Override
                 public Map createObjectContainer() {
                 return new LinkedHashMap();
                 }
                 };*/
                JSONArray tradesArray = (JSONArray) ((JSONObject) parser.parse(GetResult)).get("trades");

                for (Object obj_ : tradesArray) {
                    JSONObject obj = (JSONObject) obj_;

                    String[] time = obj.get("time").toString().split(":");
                    float price = Float.parseFloat(obj.get("price").toString());
                    float amount = Float.parseFloat(obj.get("amount").toString());
                    TradeHistoryBuySellEnum type = obj.get("type").toString().equals("买入") ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell; // 卖出, 买入

                    //http://tutorials.jenkov.com/java-date-time/java-util-timezone.html
                    // Timestamp for trades
                    Calendar cal = Calendar.getInstance(timeZone); // Huobi time
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
                    cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
                    cal.set(Calendar.SECOND, Integer.parseInt(time[2]));

                    // Initialize last purchase time if neccessary
                    if (LastPurchaseTime == 0) {
                        LastPurchaseTime = cal.getTimeInMillis() - 1; // set default param
                        cal_LastPurchaseTime = Calendar.getInstance(timeZone);
                        cal_LastPurchaseTime.setTimeInMillis(cal.getTimeInMillis());
                    }

                    //System.out.println(cal.getTime().toString());
                    // Assume things are read in ascending order
                    if (cal.getTimeInMillis() > LastPurchaseTime
                            && cal.get(Calendar.MINUTE) == cal_LastPurchaseTime.get(Calendar.MINUTE)) {
                        //System.out.println("[Trades history] Added: " + cal.getTime().toString());
                        ReturnData.merge(price, amount, cal.getTimeInMillis(), 0, type);

                        if (enableTrackTrades) {
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeSite, CurrencyPair);
                        }

                        LastPurchaseTime = cal.getTimeInMillis();

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

    /*@Override
     public TickerHistoryData parseHistoryResult(String CurrencyPair) {
     String Uri = String.format("http://market.huobi.com/staticmarket/kline%s001.html", CurrencyPair.contains("btc") ? "" : "_ltc");
     String GetResult = HttpClient.httpGet(Uri, "");
        
     if (GetResult != null) {
     String[] SplitLines = GetResult.split(",");
            
     TickerHistoryData ReturnData = new TickerHistoryData();
            
     int ReadLine = 1; // First line = huobi火币网比特币行情20140330
            
     //20131205,150500,6940,6940.2,6935,6935,7.7745,53950.7959 
     //Date, time (minutes and seconds), open, high, low, close, volume, turnover 
            
     while (ReadLine < SplitLines.length) {
     String Date = SplitLines[ReadLine + 0];
     String TimeSeconds = SplitLines[ReadLine + 1];
     String Open = SplitLines[ReadLine + 2];
     String High = SplitLines[ReadLine + 3];
     String Low = SplitLines[ReadLine + 4];
     String Close = SplitLines[ReadLine + 5];
     String Volume = SplitLines[ReadLine + 6];
     String Turnover = SplitLines[ReadLine + 7];
                
     ReadLine += 7; 
     }
     }
     return null;
     }*/
}
