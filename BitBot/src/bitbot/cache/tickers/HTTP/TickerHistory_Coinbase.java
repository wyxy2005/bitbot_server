package bitbot.cache.tickers.HTTP;

import bitbot.cache.tickers.TickerHistoryInterface;
import bitbot.cache.tickers.TickerHistoryData;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
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
public class TickerHistory_Coinbase implements TickerHistoryInterface {
    
    private final boolean enableTrackTrades;

    public TickerHistory_Coinbase(boolean enableTrackTrades) {
        this.enableTrackTrades = enableTrackTrades;
    }

    @Override
    public boolean enableTrackTrades() {
        return enableTrackTrades;
    }
    
    @Override
    public TickerHistoryData connectAndParseHistoryResult(String ExchangeCurrencyPair, String ExchangeSite, String CurrencyPair, long LastPurchaseTime, int LastTradeId) {
        String UriSell = "https://coinbase.com/api/v1/prices/sell";
        String SellResult = HttpClient.httpsGet(UriSell, "");

        String UriBuy = "https://coinbase.com/api/v1/prices/buy";
        String BuyResult = HttpClient.httpsGet(UriBuy, "");

        if (SellResult != null && BuyResult != null) {
            TickerHistoryData ReturnData = new TickerHistoryData(LastPurchaseTime, LastTradeId, 0, true);

            JSONParser parser = new JSONParser(); // Init parser
            try {
                // Timestamp for last purchase time
                //Calendar cal_LastPurchaseTime = Calendar.getInstance();

                // Container factory for the JSON array to persist the order
                ContainerFactory containerFactoryBuy = new ContainerFactory() {
                    @Override
                    public List creatArrayContainer() {
                        return new LinkedList();
                    }

                    @Override
                    public Map createObjectContainer() {
                        return new LinkedHashMap();
                    }
                };
                ContainerFactory containerFactorySell = new ContainerFactory() {
                    @Override
                    public List creatArrayContainer() {
                        return new LinkedList();
                    }

                    @Override
                    public Map createObjectContainer() {
                        return new LinkedHashMap();
                    }
                };

                LinkedHashMap buyObj = (LinkedHashMap) parser.parse(BuyResult, containerFactoryBuy);
                LinkedHashMap sellObj = (LinkedHashMap) parser.parse(SellResult, containerFactorySell);

                float buy = Float.parseFloat(((LinkedHashMap) buyObj.get("subtotal")).get("amount").toString());
                float sell = Float.parseFloat(((LinkedHashMap) sellObj.get("subtotal")).get("amount").toString());
                final TradeHistoryBuySellEnum type = TradeHistoryBuySellEnum.Unknown; // Coinbase doesn't broadcast buy or sell
                
                final long cTime = System.currentTimeMillis();

                //http://tutorials.jenkov.com/java-date-time/java-util-timezone.html
                // Timestamp for trades
                Calendar cal = Calendar.getInstance();

                //System.out.println(String.format("[Trades history] Got [%s], Buy: %f, Sell: %f", cal.getTime().toString(), buy, sell));
                ReturnData.merge_CoinbaseOrCampBX(buy, sell, cTime, type);
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
