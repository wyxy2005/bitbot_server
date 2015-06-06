package bitbot.cache.trades;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author zheng
 */
public class TradesItemData {
    
    private final float price;
    private final double amount;
    private final long LastPurchaseTime;
    private final TradeHistoryBuySellEnum type;
    
    public TradesItemData(float price, double amount, long LastPurchaseTime, TradeHistoryBuySellEnum type) {
        this.price = price;
        this.amount = amount;
        this.LastPurchaseTime = LastPurchaseTime;
        this.type = type;
    }
    
    public TradesItemData(ResultSet rs) throws SQLException {
        this.price = rs.getFloat("price");
        this.amount = rs.getDouble("amount");
        this.LastPurchaseTime = rs.getLong("LastPurchaseTime");
        this.type = TradeHistoryBuySellEnum.getEnumByValue(rs.getByte("type"));
    }
    
    public TradeHistoryBuySellEnum getType() {
        return type;
    }
    
    public long getLastPurchaseTime() {
        return LastPurchaseTime;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public float getPrice() {
        return price;
    }
}
