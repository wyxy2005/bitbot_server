package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
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
public class TickerHistory_Kraken implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_Kraken(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, long LastTradeId) {
        String Uri = String.format("https://api.kraken.com/0/public/Trades?pair=%s", CurrencyPair.replace("_", "").toUpperCase());
        String GetResult = HttpClient.httpsGet(Uri, "");

        final boolean isEthereum = CurrencyPair.contains("eth");
        final boolean isDaoXBT = CurrencyPair.contains("dao_xbt");
        final boolean isETCXBT = CurrencyPair.contains("etc_xbt");
        
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
                String pairName2 = "X" + CurrencyPair.replace("_", isDaoXBT || isEthereum || isETCXBT ? "X" : "Z" ).toUpperCase(); // XETHXXBT

                LinkedHashMap tradesMainObj = (LinkedHashMap) parser.parse(GetResult, containerFactory);
                final String error = tradesMainObj.get("error").toString();
                
                if (error.equals("[EQuery:Unknown asset pair]")) {
                    return null;
                }
                
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
                        ReturnData.merge(price, amount, date, 0, type);

                        if (enableTrackTrades) {
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeSite, CurrencyPair);
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
