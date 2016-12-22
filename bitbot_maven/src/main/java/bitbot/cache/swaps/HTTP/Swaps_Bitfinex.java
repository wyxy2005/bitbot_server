package bitbot.cache.swaps.HTTP;

import bitbot.cache.swaps.SwapsHistoryData;
import bitbot.cache.swaps.SwapsInterface;
import bitbot.handler.channel.ChannelServer;
import bitbot.util.HttpClient;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author z
 * http://docs.bitfinex.com/#provided-funding
 */
public class Swaps_Bitfinex implements SwapsInterface {

    @Override
    public SwapsHistoryData connectAndParseSwapsResult(String ExchangeSite, String Currency, String ExchangeCurrency) {
        String Uri = String.format("https://api.bitfinex.com/v1/lends/%s", Currency);
        String GetResult = HttpClient.httpsGet(Uri, "");

        if (GetResult != null) {
            JSONParser parser = new JSONParser(); // Init parser
            try {
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
                LinkedList<LinkedHashMap> lendingArray = (LinkedList<LinkedHashMap>) parser.parse(GetResult, containerFactory);

                for (LinkedHashMap obj : lendingArray) {
                    float rate = Float.parseFloat(obj.get("rate").toString());
                    double amount_lent = Double.parseDouble(obj.get("amount_lent").toString());
                    int lastupdated_timestamp = Integer.parseInt(obj.get("timestamp").toString());

                    // Use our own timestamp since we dont want last updated but the time when this is inserted to db
                    int timestamp = (int) (System.currentTimeMillis() / 1000); 
                    
                    float spot_price = 0;
                    switch (Currency) { // only get spot price when its not currency, else default value is 1
                        case "btc":
                        case "drk":
                        case "ltc": {
                            final String spotPriceStrKey = String.format("%s-%s_%s", ExchangeSite, Currency, "usd");
                            try {
                                spot_price = ChannelServer.getInstance().getWorldInterface().getInstantSpotPrice(spotPriceStrKey);
                            } catch (RemoteException exp) {
                                // Needs to check if the world server does indeed if the data
                                // Also, needs some way to store the data in memory/disk if the database fails at this point in time
                                // Exception is caught here if the world server is offline..
                            }
                            break;
                        }
                        default: {
                            spot_price = 1;
                            break;
                        }
                    }
                    System.out.println(String.format("Lending Data::: Rate = %f, Amount_Lent = %f, Timestamp = %d, Spot_Price = %f", rate, amount_lent, timestamp, spot_price));
                    
                    if (spot_price != 0) {
                        final SwapsHistoryData swapData = new SwapsHistoryData(rate, spot_price, amount_lent, timestamp, ExchangeSite, Currency, ExchangeCurrency);
                        return swapData;
                    }
                    break; // only read the latest one, so break :D
                }
            } catch (Exception parseExp) {
                //parseExp.printStackTrace();
                //System.out.println(GetResult);
                //ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask, parseExp.getMessage());
            }
        }
        return null;
    }
}
