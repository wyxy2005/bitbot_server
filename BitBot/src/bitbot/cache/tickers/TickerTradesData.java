package bitbot.cache.tickers;

import bitbot.handler.channel.ChannelServer;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.FileoutputUtil;
import bitbot.util.database.DatabaseConnection;
import bitbot.util.database.DatabaseTablesConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

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

        // Debug
        if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(LastPurchaseTime);
            String outputLog = String.format("[dd:hh:mm = (%d:%d:%d)], Price: %f, Amount: %f, Time: %d, Type: %s",
                    cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), price, amount, LastPurchaseTime, type.toString());
            FileoutputUtil.log("//" + ExchangeSite + "_" + currencyPair + ".txt", outputLog);
        }
    }

    public HistoryDatabaseCommitEnum commitDatabase() {
        // Commit data to database
    //    if (!ChannelServer.getInstance().isEnableTickerHistoryDatabaseCommit()) {
    //        return HistoryDatabaseCommitEnum.Ok;
     //   }
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
