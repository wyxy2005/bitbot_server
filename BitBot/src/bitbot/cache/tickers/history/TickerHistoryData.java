package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.mssql.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author z
 */
public class TickerHistoryData {

    private float High;
    private float Low;
    private float LastPrice;
    private double Volume; // BTC * USD
    private double Volume_Cur; // BTC/USD, cur = BTC.
    private long LastPurchaseTime;

    private String TmpExchangeSite, TmpcurrencyPair;

    public TickerHistoryData(long LastPurchaseTime) {
        this.Volume = 0;
        this.Volume_Cur = 0;
        this.High = 0;
        this.Low = Float.MAX_VALUE;
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastPrice = 0;
    }

    public HistoryDatabaseCommitState commitDatabase(long LastCommitTime, String ExchangeSite, String currencyPair) {
        if (Math.abs(LastCommitTime - LastPurchaseTime) > 60000) { // per minute
            // check if data is available
            if (Volume > 0) { // Commit for real :)
                if (!ChannelServer.getInstance().isEnableTickerHistoryDatabaseCommit()) {
                    return HistoryDatabaseCommitState.Ok;
                }
                
                String tableName;
                if (ExchangeSite != null) {
                    tableName = String.format("%s_price_%s", ExchangeSite, currencyPair);
                } else {
                    tableName = String.format("%s_price_%s", TmpExchangeSite, TmpcurrencyPair);
                }
                
                //Calendar cal = Calendar.getInstance();
                //cal.setTimeInMillis(LastPurchaseTime);
                //FileoutputUtil.log("//" + tableName + ".txt", "hh:mm = (" + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) +"), High: " + getHigh() + ", Low: " + getLow() + ", Volume: " + getVolume() + ", VolumeCur: " + getVolume_Cur());
                
                PreparedStatement ps = null;
                try {
                    Connection con = DatabaseConnection.getConnection();

                    ps = con.prepareStatement("INSERT INTO bitcoinbot." + tableName + " (high, low, vol, vol_cur, buy, server_time) VALUES (?,?,?,?,?,?);");
                    ps.setFloat(1, High);
                    ps.setFloat(2, Low);
                    ps.setDouble(3, Volume);
                    ps.setDouble(4, Volume_Cur);
                    ps.setFloat(5, LastPrice);
                    ps.setFloat(6, LastCommitTime / 1000);

                    ps.execute();
                } catch (Exception e) {
                    TmpExchangeSite = ExchangeSite; // Set reference to save later, we are fucked! database issue..
                    TmpcurrencyPair = currencyPair;

                    e.printStackTrace();
                    return HistoryDatabaseCommitState.DatabaseError;
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            return HistoryDatabaseCommitState.Ok;
        }
        return HistoryDatabaseCommitState.Time_Not_Ready;
    }

    public void merge(TickerHistoryData dataNow) {
        if (dataNow.High > High) {
            High = dataNow.High;
        }
        if (dataNow.Low < Low) {
            Low = dataNow.Low;
        }
        this.Volume_Cur += dataNow.Volume_Cur;
        this.Volume += dataNow.Volume_Cur * dataNow.High; 
        this.LastPurchaseTime = dataNow.LastPurchaseTime;
        this.LastPrice = dataNow.getLastPrice();
    }

    public void merge(float price, float amount, long LastPurchaseTime) {
        if (price > High) {
            High = price;
        }
        if (price < Low) {
            Low = price;
        }
        this.Volume_Cur += amount;
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastPrice = price;
    }
    
    public void setLastPurchaseTime(long LastPurchaseTime) {
        this.LastPurchaseTime=  LastPurchaseTime;
    }
    
    public long getLastPurchaseTime() {
        return this.LastPurchaseTime;
    }

    public void setLastPrice(float LastPrice) {
        this.LastPrice = LastPrice;
    }
    
    public float getLastPrice() {
        if (this.LastPrice == 0)
            return this.getHigh();
        return this.LastPrice;
    }
    
    public void setHigh(float High) {
        this.High = High;
    }

    public float getHigh() {
        return this.High;
    }

    public void setLow(float Low) {
        this.Low = Low;
    }

    public float getLow() {
        return this.Low;
    }

    public void setVolume(double Volume) {
        this.Volume = Volume;
    }

    public double getVolume() {
        return this.Volume;
    }
    
    public double getVolume_Cur() {
        return this.Volume_Cur;
    }
    
    public void setVolume_Cur(double Volume_Cur) {
        this.Volume_Cur = Volume_Cur;
    }
}
