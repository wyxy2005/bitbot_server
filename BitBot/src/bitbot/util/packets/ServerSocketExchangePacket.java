package bitbot.util.packets;

import bitbot.server.Constants;
import org.json.simple.JSONObject;

/**
 *
 * @author z
 */
public class ServerSocketExchangePacket {
    
    private static final String 
            Type_Connected = "ok",
            Type_PriceChanges = "instant",
            Type_Minute = "minute";
    
    public static final String getHello() {
        JSONObject obj = new JSONObject();
        
        obj.put("type", Type_Connected);
        obj.put("status", "ok");
        obj.put("serv", Constants.Server_UserAgent);
        
        return obj.toJSONString();
    }
    
    public static final String getPriceChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float last) {
        JSONObject obj = new JSONObject();
        
        obj.put("type", Type_PriceChanges);
        obj.put("status", "ok");
        
        JSONObject objData = new JSONObject();
        objData.put("exchange_pair", ExchangeCurrencyPair);
        objData.put("server_time", server_time);
        objData.put("close", close);
        objData.put("high", high);
        objData.put("low", low);
        objData.put("open", open);
        objData.put("volume", volume);
        objData.put("volume_cur", volume_cur);
        objData.put("buysell_ratio", buysell_ratio);
        objData.put("last", last);
                
        obj.put("data", objData);
        
        return obj.toJSONString();
    }
    
    public static final String getMinuteChanges(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) {
        JSONObject obj = new JSONObject();
        
        obj.put("type", Type_Minute);
        obj.put("status", "ok");
        
        JSONObject objData = new JSONObject();
        objData.put("exchange_pair", ExchangeCurrencyPair);
        objData.put("server_time", server_time);
        objData.put("close", close);
        objData.put("high", high);
        objData.put("low", low);
        objData.put("open", open);
        objData.put("volume", volume);
        objData.put("volume_cur", volume_cur);
        objData.put("buysell_ratio", buysell_ratio);
                
        obj.put("data", objData);
        
        return obj.toJSONString();
    }
}
