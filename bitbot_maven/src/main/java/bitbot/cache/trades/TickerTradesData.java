package bitbot.cache.trades;

import bitbot.cache.tickers.BacklogCommitTask_Trades;
import bitbot.cache.tickers.HistoryDatabaseCommitEnum;
import bitbot.handler.channel.ChannelServer;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.database.DatabaseConnection;
import bitbot.util.database.DatabaseTablesConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author zheng
 */
public class TickerTradesData {

    private final float price;
    private final double amount;
    private final long LastPurchaseTime;
    private final TradeHistoryBuySellEnum type;
    private final String ExchangeSite;
    private final String currencyPair;

    public TickerTradesData(float price, double amount, long LastPurchaseTime, TradeHistoryBuySellEnum type,
            String ExchangeSite, String currencyPair) {
        this.price = price;
        this.amount = amount;
        this.LastPurchaseTime = LastPurchaseTime;
        this.type = type;
        this.ExchangeSite = ExchangeSite;
        this.currencyPair = currencyPair;
    }

    public void registerForCommitQueue() {
        BacklogCommitTask_Trades.RegisterForLogging(this);
    }

    public HistoryDatabaseCommitEnum commitDatabase() {
        // Commit data to database
        if (!ChannelServer.getInstance().isEnableTickerHistoryDatabaseCommit()) {
            return HistoryDatabaseCommitEnum.Ok;
        }
        PreparedStatement ps = null;
        final String query = String.format("INSERT INTO bitcoinbot.%s (\"price\", \"amount\", \"type\", \"LastPurchaseTime\") VALUES (?,?,?,?);",
                DatabaseTablesConstants.getDatabaseTableName_Trades(ExchangeSite, currencyPair));

        try {
            Connection con = DatabaseConnection.getConnection();

            ps = con.prepareStatement(query);
            ps.setFloat(1, price);
            ps.setFloat(2, (float) amount);
            ps.setByte(3, (byte) type.getValue());
            ps.setLong(4, LastPurchaseTime);

            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
            ServerLog.RegisterForLoggingException(ServerLogType.HistoryTradesTask_DB, e);
            ServerLog.RegisterForLogging(ServerLogType.HistoryTradesTask_DB, "Query: " + query);
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
}
