package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerCacheTask;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z https://docs.gemini.com/#current-order-book
 */
public class TickerHistory_Gemini implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_Gemini(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(TickerCacheTask.TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        final String formattedExchangeName = CurrencyPair.toUpperCase().replace("_", "");
        final String Uri;

        if (LastPurchaseTime > 1) {
            Uri = String.format("https://api.gemini.com/v1/trades/%s?since=%s",
                    formattedExchangeName,
                    (LastPurchaseTime / 1000) + 1);
        } else {
            TimeZone utc = TimeZone.getTimeZone("UTC");
            Calendar cal_UTC = Calendar.getInstance(utc);
            
            Uri = String.format("https://api.gemini.com/v1/trades/%s?since=%s",
                    formattedExchangeName,
                    cal_UTC.getTimeInMillis() / 1000);
        }

        final String GetResult = HttpClient.httpsGet(Uri, "");

        if (GetResult != null) {
            final TickerHistoryData ReturnData = new TickerHistoryData(_TickerCacheTaskSource, LastPurchaseTime, LastTradeId, 0, false);

            final JSONParser parser = new JSONParser(); // Init parser
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

                for (int i = tradesArray.size() - 1; i > 0; i--) {
                    LinkedHashMap obj = tradesArray.get(i);

                    final int tradeid = Integer.parseInt(obj.get("tid").toString());
                    final long date = Integer.parseInt(obj.get("timestamp").toString()) * 1000l;
                    final float price = Float.parseFloat(obj.get("price").toString());
                    final float amount = Float.parseFloat(obj.get("amount").toString());
                    //final String exchange = obj.get("exchange").toString(); // Will always be “gemini”
                    final TradeHistoryBuySellEnum type = obj.get("type").toString().equals("buy") ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell; // buy sell

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
                /*    Calendar cal = Calendar.getInstance(); // BTCe time
                    cal.set(Calendar.YEAR, 1970);
                    cal.set(Calendar.MONTH, 0);
                    cal.set(Calendar.DATE, 0);

                    cal.add(Calendar.SECOND, (int) (date / 1000));
                        System.out.println(String.format("Got [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));*/
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                   //     System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
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
