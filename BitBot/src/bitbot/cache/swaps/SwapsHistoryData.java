package bitbot.cache.swaps;

import bitbot.cache.tickers.HistoryDatabaseCommitEnum;
import bitbot.handler.channel.ChannelServer;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.mssql.DatabaseConnection;
import bitbot.util.mssql.DatabaseTablesConstants;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author z
 */
public class SwapsHistoryData {
    
    private final int timestamp;
    private final float rate, spot_price;
    private final double amount_lent;
    private final String ExchangeSite, Currency, ExchangeCurrency;
    
    public SwapsHistoryData(float rate, float spot_price, double amount_lent, int timestamp, String ExchangeSite, String Currency, String ExchangeCurrency) {
        this.rate = rate;
        this.spot_price = spot_price;
        this.amount_lent = amount_lent;
        this.timestamp = timestamp;
        this.ExchangeSite = ExchangeSite;
        this.Currency = Currency;
        this.ExchangeCurrency = ExchangeCurrency;
    }
    
    public HistoryDatabaseCommitEnum commitDatabase() {
        // Broadcast to peerd
        broadcastDataToPeers();
        
        // Commit data to database
        if (!ChannelServer.getInstance().isEnableEnableSwapsDatabaseCommit()) {
            return HistoryDatabaseCommitEnum.Ok;
        }
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement(String.format("INSERT INTO bitcoinbot.%s (\"rate\", \"spot_price\", \"amount_lent\", \"timestamp\") VALUES (?,?,?,?);",
                    DatabaseTablesConstants.getSwapsDatabaseName(ExchangeSite, Currency)));
            ps.setFloat(1, rate);
            ps.setFloat(2, spot_price);
            ps.setDouble(3, amount_lent);
            ps.setInt(4, timestamp);

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
    
    public void broadcastDataToPeers() {
        // Broadcast to peers on other servers
        try {
            ChannelServer.getInstance().getWorldInterface().broadcastSwapData(ExchangeCurrency, rate, spot_price, amount_lent, timestamp);
        } catch (RemoteException exp) {
            ServerLog.RegisterForLoggingException(ServerLogType.RemoteError, exp);
            ChannelServer.getInstance().reconnectWorld(exp);
        } catch (NoClassDefFoundError servError) {
            // world server may have crashed or inactive :(
            System.out.println("[Warning] World Server may be inacctive or crashed. Please restart.");
            servError.printStackTrace();
        }
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
