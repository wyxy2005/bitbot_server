package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 * Documentation:
 * http://translate.google.com/translate?act=url&depth=1&hl=en&ie=UTF8&prev=_t&rurl=translate.google.com&sl=auto&tl=en&u=http://www.huobi.com/help/index.php%3Fa%3Dmarket_help
 *
 * @author z
 */
public class TickerHistory_BitVC implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_BitVC(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        String Uri;
        if (CurrencyPair.contains("Quarterly")) {
            Uri = "http://market.bitvc.com/futures/trades_btc_quarter.js";
        } else if (CurrencyPair.contains("BiWeekly")) {
            Uri = "http://market.bitvc.com/futures/trades_btc_next_week.js";
        } else {
            Uri = "http://market.bitvc.com/futures/trades_btc_week.js";
        }

        String GetResult = HttpClient.httpGet(Uri, "");

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
                LinkedList<LinkedHashMap> tradesArray = (LinkedList<LinkedHashMap>) parser.parse(GetResult, containerFactory);

                Iterator<LinkedHashMap> itr = tradesArray.iterator();
                while (itr.hasNext()) { // Loop through things in proper sequence
                    LinkedHashMap obj = itr.next();

                    long tradeid = Long.parseLong(obj.get("tid").toString());
                    long date = Integer.parseInt(obj.get("date").toString()) * 1000l;
                    float price = Float.parseFloat(obj.get("price").toString());
                    float amount = Float.parseFloat(obj.get("amount").toString());
                    TradeHistoryBuySellEnum type = obj.get("type").toString().equals("buy") ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell; // bid ask

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
                  /*  Calendar cal = Calendar.getInstance(); // BTCe time
                     cal.set(Calendar.YEAR, 1970);
                     cal.set(Calendar.MONTH, 0);
                     cal.set(Calendar.DATE, 0);
                    
                     cal.add(Calendar.HOUR, -4); // BTC-e, time 
                     cal.add(Calendar.SECOND, (int) (date / 1000));*/
                    //System.out.println(String.format("[Trades history] Got  [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                        ReturnData.merge(price, amount, date, tradeid, type);

                        if (enableTrackTrades) {
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeSite, CurrencyPair);
                        }
                    }
                }
            } catch (Exception parseExp) {
                parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
            return ReturnData;
        }
        return null;
    }
}
