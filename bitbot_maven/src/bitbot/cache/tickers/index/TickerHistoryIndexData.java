package bitbot.cache.tickers.index;

import bitbot.cache.tickers.BacklogCommitTask_Tickers;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import bitbot.util.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 *
 * @author twili
 */
public class TickerHistoryIndexData {

    private double LastPrice;
    private long LastServerUTCTime;
    private final TickerHistoryIndexCurrency currency;

    public TickerHistoryIndexData(double LastPrice, long LastServerUTCTime, TickerHistoryIndexCurrency currency) {
        this.LastServerUTCTime = LastServerUTCTime;
        this.LastPrice = LastPrice;
        this.currency = currency;
    }

    public void addToCommitBacklog() {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );
        BacklogCommitTask_Tickers.registerForImmediateLogging(this);
    }

    public boolean commitDatabase() {
        //System.out.println("Time diff: " + Math.abs(LastCommitTime - LastPurchaseTime) );

        final String query = String.format("INSERT INTO bitcoinbot.%s (\"price\", \"server_time\") VALUES (?,?);",
                String.format("index_price_%s", currency.getCurrency()));

        Connection con = DatabaseConnection.getConnection();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setDouble(1, LastPrice);
            ps.setLong(2, (long) (LastServerUTCTime / 1000l));

            ps.execute();
        } catch (Exception e) {
            //e.printStackTrace();
            ServerLog.RegisterForLoggingException(ServerLogType.HistoryIndexCacheTask_DB, e);
            ServerLog.RegisterForLogging(ServerLogType.HistoryIndexCacheTask_DB, "Query: " + query);
            return false;
        }
        return true;
    }

    public void setLastServerUTCTime(long LastServerUTCTime) {
        this.LastServerUTCTime = LastServerUTCTime;
    }

    public long getLastServerUTCTime() {
        return this.LastServerUTCTime;
    }

    public void setLastPrice(float LastPrice) {
        this.LastPrice = LastPrice;
    }

    public double getLastPrice() {
        return this.LastPrice;
    }
}
