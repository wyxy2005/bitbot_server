package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.util.mssql.DatabaseConnection;
import bitbot.util.FileoutputUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

/**
 *
 * @author z
 */
public class TickerHistoryData {

    private float Open, LastPrice, High, Low;
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
        this.Open = 0;
    }

    public HistoryDatabaseCommitEnum commitDatabase(long LastCommitTime, String ExchangeSite, String currencyPair) {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );

        if (Math.abs(LastCommitTime - LastPurchaseTime) > 60000) { // per minute
            // check if data is available
            if (Volume >= 1 && LastCommitTime >= 0) { // Commit for real :)
                String tableName;
                if (ExchangeSite != null) {
                    tableName = String.format("%s_price_%s", ExchangeSite, currencyPair);
                } else {
                    tableName = String.format("%s_price_%s", TmpExchangeSite, TmpcurrencyPair);
                }

                // Debug
                //Calendar cal = Calendar.getInstance();
                //cal.setTimeInMillis(LastPurchaseTime);
                //FileoutputUtil.log("//" + tableName + ".txt", "dd:hh:mm = (" + cal.get(Calendar.DAY_OF_MONTH) + ":" + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) +"), Open: " + getOpen() +", Close: "+getLastPrice()+" High: " + getHigh() + ", Low: " + getLow() + ", Volume: " + getVolume() + ", VolumeCur: " + getVolume_Cur());
                // End of debug
                
                if (!ChannelServer.getInstance().isEnableTickerHistoryDatabaseCommit()) {
                    return HistoryDatabaseCommitEnum.Ok;
                }
                PreparedStatement ps = null;
                try {
                    Connection con = DatabaseConnection.getConnection();

                    ps = con.prepareStatement("INSERT INTO bitcoinbot." + tableName + " (\"high\", \"low\", \"vol\", \"vol_cur\", \"open\", \"close\", \"server_time\") VALUES (?,?,?,?,?,?,?);");
                    ps.setFloat(1, High);
                    ps.setFloat(2, Low);
                    ps.setDouble(3, Volume);
                    ps.setDouble(4, Volume_Cur);
                    ps.setFloat(5, Open);
                    ps.setFloat(6, LastPrice);
                    ps.setLong(7, (long) (LastCommitTime / 1000l));

                    ps.execute();
                } catch (Exception e) {
                    if (ExchangeSite != null) { // check for null in case re-commit to backlog
                        TmpExchangeSite = ExchangeSite; // Set reference to save later, we are fucked! database issue..
                        TmpcurrencyPair = currencyPair;
                    }
                    //e.printStackTrace();
                    ServerLog.RegisterForLoggingException(ServerLogType.HistoryCacheTask_DB, e);
                    return HistoryDatabaseCommitEnum.DatabaseError;
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
            return HistoryDatabaseCommitEnum.Ok;
        }
        return HistoryDatabaseCommitEnum.Time_Not_Ready;
    }

    public void merge(TickerHistoryData dataNow) {
        if (dataNow.High > High) {
            High = dataNow.High;
        }
        if (dataNow.Low < Low) {
            Low = dataNow.Low;
        }
        if (Open == 0) {
            Open = dataNow.LastPrice;
        }
        if (Volume_Cur != 1 && Volume != 1) {
            this.Volume_Cur += dataNow.Volume_Cur;
            this.Volume += dataNow.Volume_Cur * dataNow.High;
        }
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
        if (Open == 0) {
            Open = price;
        }
        this.Volume_Cur += amount;
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastPrice = price;
    }

    public void merge_CoinbaseOrCampBX(float buy, float sell, long LastPurchaseTime) {
        if (buy > High) {
            High = buy;
        }
        if (sell < Low) {
            Low = sell;
        }
        if (Open == 0) {
            Open = buy;
        }
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastPrice = buy;

        // Set volume 1 for exchange without those data feed
        this.Volume = 1;
        this.Volume_Cur = 1;
    }

    public void setLastPurchaseTime(long LastPurchaseTime) {
        this.LastPurchaseTime = LastPurchaseTime;
    }

    public long getLastPurchaseTime() {
        return this.LastPurchaseTime;
    }

    public void setLastPrice(float LastPrice) {
        this.LastPrice = LastPrice;
    }

    public float getLastPrice() {
        if (this.LastPrice == 0) {
            return this.getHigh();
        }
        return this.LastPrice;
    }

    public void setOpen(float Open) {
        this.Open = Open;
    }

    public float getOpen() {
        return this.Open;
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
