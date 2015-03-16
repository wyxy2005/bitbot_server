package bitbot.util.packets;

import bitbot.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author z
 */
public class ServerSocketExchangePacket {
    
    private static final String 
            Type_Connected = "ok",
            Type_PriceChanges = "instant",
            Type_Minute = "minute",
            Type_Ping = "ping";
    
    public static final String getHello() {
        JSONObject obj = new JSONObject();
        
        obj.put("type", Type_Connected);
        obj.put("status", "ok");
        obj.put("serv", Constants.Server_UserAgent);
        obj.put("info", "Exchange Pair, server time, close, high, low, open, volume, volume cur, buy/sell ratio, [last] | WARNING: Session will be disconnected within 15 seconds unless authenticated ping data is provided.");
        
        return obj.toJSONString();
    }
    
    public static final String getPing() {
        JSONObject obj = new JSONObject();
        
        obj.put("type", Type_Ping);
        obj.put("status", "ok");
        obj.put("serv", Constants.Server_UserAgent);
        obj.put("info", "");
        
        return obj.toJSONString();
    }
    
    public static final String getPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) {
        JSONObject obj = new JSONObject();

        JSONArray array = new JSONArray();
        array.add(ExchangeCurrencyPair);
        array.add(server_time);
        array.add(close);
        array.add(high);
        array.add(low);
        array.add(open);
        array.add(volume);
        array.add(volume_cur);
        array.add(buysell_ratio);
        array.add(last);
                
        obj.put("data", array);
        
        obj.put("type", Type_PriceChanges);
        
        return obj.toJSONString();
    }
    
    public static final String getMinuteChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) {
        JSONObject obj = new JSONObject();
        
        JSONArray array = new JSONArray();
        array.add(ExchangeCurrencyPair);
        array.add(server_time);
        array.add(close);
        array.add(high);
        array.add(low);
        array.add(open);
        array.add(volume);
        array.add(volume_cur);
        array.add(buysell_ratio);
                
        obj.put("data", array);
        
        obj.put("type", Type_Minute);
        
        return obj.toJSONString();
    }
}
