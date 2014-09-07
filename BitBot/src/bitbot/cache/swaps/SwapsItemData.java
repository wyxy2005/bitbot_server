package bitbot.cache.swaps;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author z
 */
public class SwapsItemData {
    
    private final int timestamp;
    private final float rate, spot_price;
    private final double amount_lent;
    
    public SwapsItemData(float rate, float spot_price, double amount_lent, int timestamp) {
        this.rate = rate;
        this.spot_price = spot_price;
        this.amount_lent = amount_lent;
        this.timestamp = timestamp;
    }
    
    public SwapsItemData(ResultSet rs) throws SQLException {
        this.timestamp = rs.getInt("timestamp");//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.rate = rs.getFloat("rate");
        this.amount_lent = rs.getDouble("amount_lent");
        this.spot_price = rs.getFloat("spot_price");
    }
    
    public int getTimestamp() {
        return timestamp;
    }
    
    public float getRate() {
        return rate;
    }
    
    public float getSpotPrice() {
        return spot_price;
    }
    
    public double getAmountLent() {
        return amount_lent;
    }
}
