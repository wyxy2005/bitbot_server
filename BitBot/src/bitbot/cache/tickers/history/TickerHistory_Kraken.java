package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
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
public class TickerHistory_Kraken implements TickerHistory {

   // private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT+6");
    private long lastBroadcastedTime = 0;

    private boolean readyToBroadcastPriceChanges() {
        final long cTime = System.currentTimeMillis();
        if (cTime - lastBroadcastedTime > 2000) {
            lastBroadcastedTime = cTime;
            return true;
        }
        return false;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        String Uri = String.format("https://api.kraken.com/0/public/Trades?pair=%s", CurrencyPair.replace("_", "").toUpperCase());
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
                String pairName2 = "X" + CurrencyPair.replace("_", "Z").toUpperCase();

                LinkedHashMap tradesMainObj = (LinkedHashMap) parser.parse(GetResult, containerFactory);
                LinkedList<LinkedList> resultMainObj = (LinkedList) ((LinkedHashMap) tradesMainObj.get("result")).get(pairName2);

                for (int i = resultMainObj.size() - 1; i >= 0; i--) {
                    LinkedList obj = resultMainObj.get(i);

                    float price = Float.parseFloat(obj.get(0).toString());
                    float amount = Float.parseFloat(obj.get(1).toString());
                    long date = (long) Double.parseDouble(obj.get(2).toString()) * 1000l;
                    TradeHistoryBuySellEnum type = obj.get(3).toString().equals("b") ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell; // buy sell
                    //String unk = obj.get(4).toString(); // Not sure what this is... returns 'l'

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
                    
                     //System.out.println(String.format("[Trades history] Got [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));*/
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", date, price, amount));
                        ReturnData.merge(price, amount, date, 0);

                        if (readyToBroadcastPriceChanges()) {
                            ChannelServer.getInstance().broadcastPriceChanges(
                                    type,
                                    CurrencyPair,
                                    price,
                                    amount,
                                    date,
                                    0);
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
