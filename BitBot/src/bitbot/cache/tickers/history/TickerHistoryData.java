package bitbot.cache.tickers.history;

import bitbot.handler.channel.ChannelServer;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.util.mssql.DatabaseConnection;
import bitbot.util.FileoutputUtil;
import java.rmi.RemoteException;
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
    private int LastTradeId;

    private boolean isCoinbase_CampBX = false;

    private boolean isDatasetReadyForCommit = false; // additional boolean to ensure that future changes won't bug this up.. 
    private String TmpExchangeSite, TmpcurrencyPair;

    public TickerHistoryData(long LastPurchaseTime, int LastTradeId, float LastPrice, boolean IsCoinbaseOrCexIO) {
        this.High = 0;
        this.Low = Float.MAX_VALUE;
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastTradeId = LastTradeId;
        this.LastPrice = 0;
        this.Open = LastPrice;
        this.isCoinbase_CampBX = IsCoinbaseOrCexIO;

        if (IsCoinbaseOrCexIO) {
            this.Volume = 1;
            this.Volume_Cur = 1;
        } else {
            this.Volume = 0;
            this.Volume_Cur = 0;
        }
    }

    public HistoryDatabaseCommitEnum tryCommitDatabase(long LastCommitTime, String ExchangeSite, String currencyPair) {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );

        if (Math.abs(LastCommitTime - LastPurchaseTime) > 60000) { // per minute
            // check if data is available
            if (Volume > 0 && LastPurchaseTime > 0 && High > 0 && Low > 0) { // Commit for real :)
                if (ExchangeSite != null) {
                    TmpExchangeSite = ExchangeSite; // Set reference for backlog, when needed
                    TmpcurrencyPair = currencyPair;
                }
                isDatasetReadyForCommit = true;
                BacklogCommitTask.RegisterForImmediateLogging(this);
            }
            return HistoryDatabaseCommitEnum.Ok;
        }
        return HistoryDatabaseCommitEnum.Time_Not_Ready;
    }

    public HistoryDatabaseCommitEnum commitDatabase(long LastCommitTime, String ExchangeSite, String currencyPair) {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );
        if (!isDatasetReadyForCommit || TmpExchangeSite == null || TmpcurrencyPair == null) {
            return HistoryDatabaseCommitEnum.Time_Not_Ready;
        }
        String tableName = String.format("%s_price_%s", TmpExchangeSite, TmpcurrencyPair);

        // Debug
        if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(LastPurchaseTime);
            String outputLog = String.format("dd:hh:mm = (%d:%d:%d), Open: %f, Close: %f, High: %f, Low: %f, Volume: %f, VolumeCur: %f",
                    cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), getOpen(), getLastPrice(), getHigh(), getLow(), getVolume(), getVolume_Cur());
            FileoutputUtil.log("//" + tableName + ".txt", outputLog);
        }

        // Broadcast to peers on other servers
        try {
            final String ExchangeCurrencyPair = String.format("%s-%s", TmpExchangeSite, TmpcurrencyPair);
            ChannelServer.getInstance().getWorldInterface().broadcastNewGraphEntry(ExchangeCurrencyPair, LastPurchaseTime / 1000l, LastPrice, High, Low, Open, Volume, Volume_Cur);
        } catch (RemoteException exp) {
            ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);
        }
        
        // Commit data to database
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
            ps.setLong(7, (long) (LastPurchaseTime / 1000l));

            ps.execute();
        } catch (Exception e) {
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
        return HistoryDatabaseCommitEnum.Ok;
    }

    public void merge(TickerHistoryData dataNow) {
        if (dataNow.High > this.High) {
            this.High = dataNow.High;
        }
        if (dataNow.Low < this.Low) {
            this.Low = dataNow.Low;
        }
        if (this.Open == 0) {
            this.Open = dataNow.LastPrice;
        }
        if (!isCoinbase_CampBX && this.Volume_Cur != 1d && this.Volume != 1d) {
            this.Volume_Cur += dataNow.Volume_Cur;
            this.Volume += dataNow.Volume_Cur * dataNow.High;
        }
        this.LastPurchaseTime = dataNow.LastPurchaseTime;
        if (dataNow.getLastTradeId() != 0)
            this.LastTradeId = dataNow.getLastTradeId();
        this.LastPrice = dataNow.getLastPrice();
    }

    public void merge(float price, float amount, long LastPurchaseTime, int LastTradeId) {
        if (price > this.High) {
            this.High = price;
        }
        if (price < this.Low) {
            this.Low = price;
        }
        if (this.Open == 0) {
            this.Open = price;
        }
        if (this.Volume_Cur != 1 && this.Volume != 1) {
            this.Volume_Cur += amount;
            this.Volume += amount * High;
        }
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastTradeId = LastTradeId;
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
    
    public void setLastTradeId(int LastTradeId) {
        this.LastTradeId = LastTradeId;
    }

    public int getLastTradeId() {
        return this.LastTradeId;
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

    public boolean isCoinbase_CampBX() {
        return isCoinbase_CampBX;
    }

    public boolean isDatasetReadyForCommit() {
        return isDatasetReadyForCommit;
    }
}
