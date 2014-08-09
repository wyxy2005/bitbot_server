package bitbot.cache.tickers.history;

import bitbot.util.HttpClient;
import java.util.Calendar;
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
public class TickerHistory_Cryptsy implements TickerHistory {
    //private static final TimeZone timeZone = TimeZone.getTimeZone("Etc/GMT-6");

    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        // General market data
        String Uri = String.format("http://pubapi.cryptsy.com/api.php?method=singlemarketdata&marketid=%d", getCryptsyMarketId(CurrencyPair));
        String GetResult = HttpClient.httpGet(Uri, "");

        long newPurchaseDate = LastPurchaseTime;
        
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
                LinkedHashMap data = (LinkedHashMap) parser.parse(GetResult, containerFactory);
                int success = Integer.parseInt(data.get("success").toString());

                if (success == 1) {
                    String[] TickerReadable = CurrencyPair.toUpperCase().split("_");

                    LinkedHashMap returnData = (LinkedHashMap) data.get("return");
                    LinkedHashMap marketsData = (LinkedHashMap) returnData.get("markets");
                    LinkedHashMap pairMarketData = (LinkedHashMap) marketsData.get(TickerReadable[0]);
                    LinkedList<LinkedHashMap> recenttrades = (LinkedList<LinkedHashMap>) pairMarketData.get("recenttrades");

                    Iterator<LinkedHashMap> itr = recenttrades.iterator();
                    while (itr.hasNext()) { // Loop through things in proper sequence
                        LinkedHashMap obj = itr.next();

                        // Parse time
                        String time = obj.get("time").toString();  // 2014-07-28 05:16:40
                        String[] dateTimeSplit = time.split(" ");

                        String[] dateSplit = dateTimeSplit[0].split("-");
                        String[] timeSplit = dateTimeSplit[1].split(":");

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR, Integer.parseInt(dateSplit[0]));
                        cal.set(Calendar.MONTH, Integer.parseInt(dateSplit[1]));
                        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateSplit[2]));

                        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeSplit[0]));
                        cal.set(Calendar.MINUTE, Integer.parseInt(timeSplit[1]));
                        cal.set(Calendar.SECOND, Integer.parseInt(timeSplit[2]));

                        // Etc
                        int tradeid = Integer.parseInt(obj.get("id").toString());
                        float price = Float.parseFloat(obj.get("price").toString());
                        float amount = Float.parseFloat(obj.get("quantity").toString());
                        TradeHistoryBuySellEnum type = obj.get("type").toString().equals("Buy") ? TradeHistoryBuySellEnum.Buy : TradeHistoryBuySellEnum.Sell;

                        // Initialize last purchase time if neccessary
                        if (LastPurchaseTime == 0) {
                            LastPurchaseTime = cal.getTimeInMillis() - 1; // set default param
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
                        //System.out.println(String.format("[Trades history] Got  [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));

                        // Assume things are read in ascending order
                        if (cal.getTimeInMillis() > LastPurchaseTime) {
                            //System.out.println(String.format("[Trades history] Added [%s], Price: %f, Sum: %f ", cal.getTime().toString(), price, amount));
                            ReturnData.merge(price, amount, cal.getTimeInMillis(), tradeid, type);

                            // Update the biggest purchase time
                            newPurchaseDate = Math.max(LastPurchaseTime, cal.getTimeInMillis());
                        }
                    }
                }
            } catch (Exception parseExp) {
                //parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
            ReturnData.setLastPurchaseTime(newPurchaseDate);
            return ReturnData;
        }
        return null;
    }

    public static int getCryptsyMarketId(String pair) {
        switch (pair) {
            case "ltc_usd":
                return 1;
            case "btc_usd":
                return 2;
            case "ltc_btc":
                return 3;
            case "ftc_usd":
                return 6;
            case "nxt_btc":
                return 159;
            case "doge_usd":
                return 182;
            case "drk_usd":
                return 213;
            case "rdd_usd":
                return 262;
        }
        return -1;
    }
}
