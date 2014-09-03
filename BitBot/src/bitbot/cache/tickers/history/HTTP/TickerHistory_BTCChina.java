package bitbot.cache.tickers.history.HTTP;

import bitbot.cache.tickers.history.TickerHistory;
import bitbot.cache.tickers.history.TickerHistoryData;
import bitbot.cache.tickers.history.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
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
public class TickerHistory_BTCChina implements TickerHistory {
    //private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT-6");

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        String[] split = CurrencyPair.split("_");

        String Uri = String.format("https://data.btcchina.com/data/historydata?market=%s&since=%d", (split[1] + split[0]).toUpperCase(), LastTradeId);
        String GetResult = HttpClient.httpsGet(Uri, "");

        if (GetResult != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(LastPurchaseTime, LastTradeId, 0, false);

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

                for (int i = tradesArray.size() - 1; i >= 0; i--) {
                    LinkedHashMap obj = tradesArray.get(i);

                    int tradeid = Integer.parseInt(obj.get("tid").toString());
                    long date = Long.parseLong(obj.get("date").toString()) * 1000;
                    float price = Float.parseFloat(obj.get("price").toString());
                    float amount = Float.parseFloat(obj.get("amount").toString());
                    final TradeHistoryBuySellEnum type = obj.get("type") == "buy" ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell;

                    // Initialize last purchase time if neccessary
                    if (LastTradeId == 0) {
                        /*Calendar cal_LastPurchaseTime = Calendar.getInstance();
                         cal_LastPurchaseTime.set(Calendar.YEAR, 1970);
                         cal_LastPurchaseTime.set(Calendar.MONTH, 0);
                         cal_LastPurchaseTime.set(Calendar.DATE, 0);
                        
                         cal_LastPurchaseTime.add(Calendar.HOUR, 8);
                         cal_LastPurchaseTime.add(Calendar.SECOND, (int) (date / 1000));*/

                        LastPurchaseTime = date - 1;//cal_LastPurchaseTime.getTimeInMillis(); // set default param
                        ReturnData.setLastPurchaseTime(LastPurchaseTime);
                        
                        LastTradeId = tradeid - 1;
                        ReturnData.setLastTradeId(LastTradeId);
                    }

                    //http://tutorials.jenkov.com/java-date-time/java-util-timezone.html
                    // Timestamp for trades
                    /*Calendar cal = Calendar.getInstance(); // BTCe time
                     cal.set(Calendar.YEAR, 1970);
                     cal.set(Calendar.MONTH, 0);
                     cal.set(Calendar.DATE, 0);

                     cal.add(Calendar.SECOND, (int) (date / 1000));*/
                    
                    // System.out.println(String.format("[Trades history] Got [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                    // Assume things are read in ascending order
                    if (tradeid > LastTradeId) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                        ReturnData.merge(price, amount, date, tradeid, type);
                    }
                }
            } catch (Exception parseExp) {
                //parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
            return ReturnData;
        }
        return null;
    }
}
