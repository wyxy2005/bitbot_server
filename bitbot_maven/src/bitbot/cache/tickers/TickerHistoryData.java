package bitbot.cache.tickers;

import bitbot.cache.tickers.TickerCacheTask.TickerCacheTask_ExchangeHistory;
import bitbot.cache.trades.TickerTradesData;
import bitbot.cache.trades.TradeHistoryBuySellEnum;
import bitbot.handler.channel.ChannelServer;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.database.DatabaseConnection;
import bitbot.util.FileoutputUtil;
import bitbot.util.database.DatabaseTablesConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author z
 */
public class TickerHistoryData {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    private float Open, LastPrice, High, Low;
    private double Volume; // BTC * USD
    private double Volume_Cur; // BTC/USD, cur = BTC.
    private long LastPurchaseTime;
    private long LastServerUTCTime;
    private long LastTradeId;

    private double TotalBuyVolume = 1, TotalSellVolume = 1;

    private boolean isCoinbase_CampBX = false;

    private boolean isDatasetReadyForCommit = false; // additional boolean to ensure that future changes won't bug this up.. 
    
    // Keep a reference of the Runnable source, that way we dont need to create a new pair of string CurrencyPair, ExchangeSite, ExchangeCurrencyPair
    // Since reference is much cheaper ;) 
    private TickerCacheTask_ExchangeHistory _TickerCacheTaskSource;
    
    public TickerHistoryData(TickerCacheTask_ExchangeHistory _TickerCacheTaskSource, long LastPurchaseTime, long LastTradeId, float LastPrice, boolean IsCoinbaseOrCexIO) {
        this.High = 0;
        this.Low = Float.MAX_VALUE;
        this.LastPurchaseTime = LastPurchaseTime;
        this.LastTradeId = LastTradeId;
        this.LastPrice = 0;
        this.Open = LastPrice;
        this.isCoinbase_CampBX = IsCoinbaseOrCexIO;
        this._TickerCacheTaskSource = _TickerCacheTaskSource;

        if (IsCoinbaseOrCexIO) {
            this.Volume = 1;
            this.Volume_Cur = 1;
        } else {
            this.Volume = 0;
            this.Volume_Cur = 0;
        }
    }

    public HistoryDatabaseCommitEnum tryCommitDatabase(long LastCommitTime, String ExchangeCurrencyPair, boolean readyToBroadcastPriceChanges) {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );

        // The last time data was merged from exchanges
        final Calendar cal_LastCommitTime = Calendar.getInstance();
        cal_LastCommitTime.setTimeInMillis(LastCommitTime);

        // The current time
        final Calendar cal_CurrentTime = Calendar.getInstance();
        cal_CurrentTime.setTimeInMillis(LastPurchaseTime);
        
        if (cal_LastCommitTime.get(Calendar.MINUTE) != cal_CurrentTime.get(Calendar.MINUTE) &&
                Math.abs(LastCommitTime - LastPurchaseTime) >= 3000) {
            
            // check if data is available
            if (Volume > 0 && LastPurchaseTime > 0 && High > 0 && Low > 0 && !isDatasetReadyForCommit) { // Commit for real :)
                isDatasetReadyForCommit = true;

                // Check again, just in case
                if (this.LastServerUTCTime == 0) {
                    throw new RuntimeException("LastServerUTCTime is not set...");
                } else if (_TickerCacheTaskSource == null) {
                    throw new RuntimeException("_TickerCacheTaskSource is null");
                }

               // truncateLastServerUTCMinute();
                
                // Broadcast to peers on other servers
                broadcastCompletedMinuteCandleDataToPeers();
                
                BacklogCommitTask_Tickers.RegisterForImmediateLogging(this);

                // Debug
                if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
                    final Calendar cal_LastMergeTime = Calendar.getInstance();
                    cal_LastMergeTime.setTimeInMillis(this.LastServerUTCTime);

                    String outputLog = String.format("[dd:hh:mm = (%d:%d:%d)], Open: %f, Close: %f, High: %f, Low: %f, Volume: %f, VolumeCur: %f, Ratio: %f",
                            cal_LastMergeTime.get(Calendar.DAY_OF_MONTH),
                            cal_LastMergeTime.get(Calendar.HOUR),
                            cal_LastMergeTime.get(Calendar.MINUTE), getOpen(), getLastPrice(), getHigh(), getLow(), getVolume(), getVolume_Cur(), TotalBuyVolume / TotalSellVolume);
                    FileoutputUtil.log("//" + ExchangeCurrencyPair + ".txt", outputLog);
                }
            }

            return HistoryDatabaseCommitEnum.Ok;
        }
        // Broadcast this piece of data to world server 
        if (getLastPrice() != 0 && readyToBroadcastPriceChanges) {
            ChannelServer.getInstance().broadcastPriceChanges(
                    ExchangeCurrencyPair,
                    getLastServerUTCTime() / 1000l,
                    getLastPrice(), // using last price as close since this isnt known yet
                    getHigh(), getLow(), getOpen(),
                    getVolume(), getVolume_Cur(),
                    getBuySell_Ratio(),
                    getLastPrice()
            );
        }

        return HistoryDatabaseCommitEnum.Time_Not_Ready;
    }

    public HistoryDatabaseCommitEnum commitDatabase(long LastCommitTime, String ExchangeSite, String currencyPair) {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );
        if (!isDatasetReadyForCommit) {
            return HistoryDatabaseCommitEnum.Time_Not_Ready;
        }
        // Commit data to database
        if (!ChannelServer.getInstance().isEnableTickerHistoryDatabaseCommit()) {
            return HistoryDatabaseCommitEnum.Ok;
        }
        PreparedStatement ps = null;
        final String query = String.format("INSERT INTO bitcoinbot.%s (\"high\", \"low\", \"vol\", \"vol_cur\", \"open\", \"close\", \"server_time\", \"buysell_ratio\") VALUES (?,?,?,?,?,?,?,?);",
                DatabaseTablesConstants.getDatabaseTableName(_TickerCacheTaskSource.getExchangeSite(), _TickerCacheTaskSource.getCurrencyPair()));

        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement(query);
            ps.setFloat(1, High);
            ps.setFloat(2, Low);
            ps.setDouble(3, Volume);
            ps.setDouble(4, Volume_Cur);
            ps.setFloat(5, Open);
            ps.setFloat(6, LastPrice);
            ps.setLong(7, (long) (LastServerUTCTime / 1000l));
            ps.setFloat(8, getBuySell_Ratio()); // ratio

            ps.execute();
        } catch (Exception e) {
            //e.printStackTrace();
            ServerLog.RegisterForLoggingException(ServerLogType.HistoryCacheTask_DB, e);
            ServerLog.RegisterForLogging(ServerLogType.HistoryCacheTask_DB, "Query: " + query);
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
        _TickerCacheTaskSource = null; // cleanup
        
        return HistoryDatabaseCommitEnum.Ok;
    }

    /**
     * Only tracking a couple of BTC pairs with massive volume
     *
     * @param price
     * @param amount
     * @param LastPurchaseTime
     * @param type
     * @param ExchangeSite
     * @param currencyPair
     */
    public void trackAndRecordLargeTrades(float price, double amount, long LastPurchaseTime, TradeHistoryBuySellEnum type,
            String ExchangeSite, String currencyPair) {

        if (ChannelServer.getInstance().isEnableTradesDatabaseCommit()) {
            if (amount >= ChannelServer.getInstance().getRequiredTradeSizeForTradesLogging()) {
                final TickerTradesData data = new TickerTradesData(price, amount, LastPurchaseTime / 1000, type, ExchangeSite, currencyPair);

                data.registerForCommitQueue(); // no need to reference elsewhere

                // Broadcast trades data to peers on other servers
                try {
                    final String ExchangeCurrencyPair = String.format("%s-%s", ExchangeSite, currencyPair);

                    ChannelServer.getInstance().getWorldInterface().broadcastNewTradesEntry(
                            ExchangeCurrencyPair,
                            price,
                            amount,
                            LastPurchaseTime,
                            (byte) type.getValue());
                } catch (Exception exp) {
                    ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);
                    ChannelServer.getInstance().reconnectWorld(exp);
                }
            }
        }

        // Debug
        if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(LastPurchaseTime);
            String outputLog = String.format("[dd:hh:mm = (%d:%d:%d)], Price: %f, Amount: %f, Time: %d, Type: %s",
                    cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), price, amount, LastPurchaseTime, type.toString());
            FileoutputUtil.log("//" + ExchangeSite + "-" + currencyPair + ".txt", outputLog);
        }
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
            this.Volume += dataNow.Volume;
            this.TotalBuyVolume += dataNow.TotalBuyVolume;
            this.TotalSellVolume += dataNow.TotalSellVolume;
        }
        this.LastPurchaseTime = dataNow.LastPurchaseTime;
        this.LastServerUTCTime = dataNow.getLastServerUTCTime();
        if (dataNow.getLastTradeId() != 0) {
            this.LastTradeId = dataNow.getLastTradeId();
        }
        this.LastPrice = dataNow.getLastPrice();
    }

    public void merge(float price, float amount, long LastPurchaseTime, long LastTradeId, TradeHistoryBuySellEnum type) {
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
            this.Volume += amount * price;

            switch (type) {
                case Buy: {
                    this.TotalBuyVolume += Volume_Cur;
                    break;
                }
                case Sell: {
                    this.TotalSellVolume += Volume_Cur;
                    break;
                }
            }
        }
        if (this.LastPurchaseTime < LastPurchaseTime) {
            this.LastPurchaseTime = LastPurchaseTime;
        }
        if (this.LastTradeId < LastTradeId) {
            this.LastTradeId = LastTradeId;
        }
        this.LastPrice = price;
    }

    public void merge_CoinbaseOrCampBX(float buy, float sell, long LastPurchaseTime, TradeHistoryBuySellEnum type) {
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
        this.TotalBuyVolume = 1;
        this.TotalBuyVolume = 1;
    }

    public void broadcastCompletedMinuteCandleDataToPeers() {
        // Broadcast to peers on other servers
        try {
            final String ExchangeCurrencyPair = String.format("%s-%s", _TickerCacheTaskSource.getExchangeSite(), _TickerCacheTaskSource.getCurrencyPair());
           // Calendar cal_UTC = Calendar.getInstance(UTC);
            
            ChannelServer.getInstance().getWorldInterface().broadcastNewGraphEntry(
                    ExchangeCurrencyPair, 
                    LastServerUTCTime / 1000l, 
                 //   cal_UTC.getTimeInMillis() / 1000l, // Have to broadcast current time, otherwise TV charts will not be updated
                    LastPrice, 
                    High, 
                    Low, 
                    Open, 
                    Volume, 
                    Volume_Cur, 
                    getBuySell_Ratio());
        } catch (Exception exp) {
            ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);
            ChannelServer.getInstance().reconnectWorld(exp);
        }
    }

    public void setLastPurchaseTime(long LastPurchaseTime) {
        this.LastPurchaseTime = LastPurchaseTime;
    }

    public long getLastPurchaseTime() {
        return this.LastPurchaseTime;
    }

    public void setLastServerUTCTime(long LastServerUTCTime) {
        this.LastServerUTCTime = LastServerUTCTime;
    }

    public long getLastServerUTCTime() {
        return this.LastServerUTCTime;
    }
    
   /* private void truncateLastServerUTCMinute() {
        // 1450247285000
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(LastServerUTCTime);
        
        //cal.add(Calendar.MINUTE, 1);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        this.LastServerUTCTime = cal.getTimeInMillis();
    }*/

    public float getBuySell_Ratio() {
        return (float) (TotalBuyVolume / TotalSellVolume);
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

    public void setLastTradeId(long LastTradeId) {
        this.LastTradeId = LastTradeId;
    }

    public long getLastTradeId() {
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
