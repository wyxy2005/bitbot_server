package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.util.HttpClient;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author zheng
 */
public class TickerHistory_CoinbaseExchange implements TickerHistoryInterface {

    private final boolean enableTrackTrades;

    public TickerHistory_CoinbaseExchange(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT+0:00"));
    }

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        String Uri = String.format("https://api.exchange.coinbase.com/products/%s/trades", CurrencyPair.toUpperCase().replace("_", "-")); // 2015-01-29 10:58:56.370375+00
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

                Iterator<LinkedHashMap> itr = tradesArray.iterator();
                while (itr.hasNext()) { // Loop through things in proper sequence
                    LinkedHashMap obj = itr.next();

                    int tradeid = Integer.parseInt(obj.get("trade_id").toString());
                    String strdate = obj.get("time").toString();  // 2015-01-30 11:00:44.583563+00
                    float price = Float.parseFloat(obj.get("price").toString());
                    float amount = Float.parseFloat(obj.get("size").toString());
                    TradeHistoryBuySellEnum type = obj.get("side").toString().equals("sell") ? TradeHistoryBuySellEnum.Sell : TradeHistoryBuySellEnum.Buy; // bid ask

                    // parse coinbase's annoying date format
                    Date dateobj = df.parse(strdate); // 2015-01-30 11:00:44.583563+00
                    long date = dateobj.getTime();

                    // Initialize last purchase time if neccessary
                    if (LastPurchaseTime == 0) {
                        LastPurchaseTime = date - 1; // set default param

                        ReturnData.setLastPurchaseTime(LastPurchaseTime);
                    }

                    //System.out.println(String.format("[Trades history] Got  [%s], Price: %f, Sum: %f ", dateobj.toString(), price, amount));
                    // Assume things are read in ascending order
                    if (date > LastPurchaseTime) {
                        //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", date, price, amount));
                        ReturnData.merge(price, amount, date, tradeid, type);

                        if (enableTrackTrades) {
                            ReturnData.trackAndRecordLargeTrades(price, amount, LastPurchaseTime, type, ExchangeSite, CurrencyPair);
                        }
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
