package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
import bitbot.util.HttpClient;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 */
public class TickerHistory_ItBit implements TickerHistory {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime) {
        String Uri = String.format("https://www.itbit.com/api/v2/markets/%s/trades?since=%d", CurrencyPair.replace("_", "").toUpperCase(), LastPurchaseTime);
        String GetResult = HttpClient.httpsGet(Uri, "");

        if (GetResult != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(LastPurchaseTime);

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
                LinkedList<LinkedHashMap> tradesMainObj = (LinkedList<LinkedHashMap>) parser.parse(GetResult, containerFactory);

                for (LinkedHashMap obj : tradesMainObj)
                {
                    float amount = Float.parseFloat(obj.get("amount").toString());
                    float price = Float.parseFloat(obj.get("price").toString());
                    long date = (long) Double.parseDouble(obj.get("date").toString()) * 1000l;
                    //String type = // Not known
                    int tid = Integer.parseInt(obj.get("tid").toString());

                    // Initialize last purchase time if neccessary
                    if (LastPurchaseTime == 0) {
                        LastPurchaseTime = date; // set default param
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
                    
                    System.out.println(String.format("[Trades history] Got [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                    */
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", date, price, amount));
                        ReturnData.merge(price, amount, date);
                        
                        ChannelServer.getInstance().BroadcastConnectedClients(
                                TradeHistoryBuySellEnum.Unknown,
                                CurrencyPair,
                                tid);
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
