package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
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
 *
 * @author twili_000
 */
public class TickerHistory_Dgex implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_Dgex(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        //String Uri = "https://dgex.com/API/trades.json";
        String Uri = "https://dgex.com/API/trades3h.json";
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
                LinkedList<LinkedHashMap> tradesArray = (LinkedList<LinkedHashMap>) ((LinkedHashMap) parser.parse(GetResult, containerFactory)).get("ticker");

                Iterator<LinkedHashMap> itr = tradesArray.iterator();
                while (itr.hasNext()) { // Loop through things in proper sequence
                    LinkedHashMap obj = itr.next();

                    long tradeid = 0;
                    long date = Integer.parseInt(obj.get("timestamp").toString()) * 1000l;
                    float price = Float.parseFloat(obj.get("unitprice").toString());
                    float amount = Float.parseFloat(obj.get("units").toString());
                    TradeHistoryBuySellEnum type = TradeHistoryBuySellEnum.Unknown; // dgex doesn't broadcast buy or sell
                    //String type = obj.get("trade_type").toString(); // bid/ask

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
                    /*Calendar cal = Calendar.getInstance(); // BTCe time
                     cal.set(Calendar.YEAR, 1970);
                     cal.set(Calendar.MONTH, 0);
                     cal.set(Calendar.DATE, 0);
                    
                     cal.add(Calendar.HOUR, -4); // BTC-e, time 
                     cal.add(Calendar.SECOND, (int) (date / 1000));
                    
                     System.out.println(String.format("[Trades history] Got  [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                     */
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                        ReturnData.merge(price, amount, date, tradeid, type);

                        if (enableTrackTrades) {
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeCurrencyPair, CurrencyPair);
                        }
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
