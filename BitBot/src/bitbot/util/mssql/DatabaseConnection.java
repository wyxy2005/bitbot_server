package bitbot.util.mssql;

import bitbot.util.encryption.Base64;
import bitbot.util.Randomizer;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author z
 */
public class DatabaseConnection {

    private static String DecryptedDatabaseStrings = null;
    private static String DecryptedDatabaseStrings_u = null;
    private static String DecryptedDatabaseStrings_p = null;

    private static long connectionTimeOut = 5 * 60 * 1000; // 5 minutes
    private static long TIMEOUT_CACHE = -1;
    private static final Object mutex = new Object();
    private static final Map<Long, ConWrapper> connections = new HashMap();

    static {
        String encrypted = "amRiYzpzcWxzZXJ2ZXI6Ly9mam95bnA0bG51LmRhdGFiYXNlLndpbmRvd3MubmV0OjE0MzM7ZGF0YWJhc2U9Qml0Q29pbkFUcHFBQ1hBdTtlbmNyeXB0PXRydWU7aG9zdE5hbWVJbkNlcnRpZmljYXRlPSouZGF0YWJhc2Uud2luZG93cy5uZXQ7bG9naW5UaW1lb3V0PTMwOw==";
        String encrypted_u = "c2ZfMzRtZG9sc3NhZDk0NW0=";
        String encrypted_p = "MzEyZGZkNGY1OjpAJGRnZ2dmZzQ1NDU1NDY=";
        
        //System.out.println(Base64.encodeBytes("jdbc:sqlserver://fjoynp4lnu.database.windows.net:1433;database=BitCoinATpqACXAu;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;".getBytes()));

        //DecryptedDatabaseStrings = "jdbc:jtds:sqlserver://fjoynp4lnu.database.windows.net:1433;DatabaseName=BitCoinATpqACXAu;useNTLMv2=true;useJCIFS=true;tcpNoDelay=true;instance=SQLEXPRESS;ssl=require";
        //DecryptedDatabaseStrings = "jdbc:sqlserver://fjoynp4lnu.database.windows.net:1433;database=BitCoinATpqACXAu;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";

        try {
            DecryptedDatabaseStrings = new String(Base64.decode(encrypted));
            DecryptedDatabaseStrings_u = new String(Base64.decode(encrypted_u));
            DecryptedDatabaseStrings_p = new String(Base64.decode(encrypted_p));
        } catch (IOException exp) {
            DecryptedDatabaseStrings = "";
        }
    }

    public static Connection getConnection2() {
// Setup the connection with the DB
        Connection connect = null;
        try {
            //Class.forName("net.sourceforge.jtds.jdbc.Driver");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            connect = DriverManager.getConnection(
                DecryptedDatabaseStrings, DecryptedDatabaseStrings_u, DecryptedDatabaseStrings_p);
        } catch (SQLException|ClassNotFoundException exp) {
            exp.printStackTrace();
        }
        return connect;
    }

    public static final Connection getConnection() {
        if (Randomizer.nextInt(100) == 0) {
            new Thread(new ClearingTask()).start();
        }
        final Thread cThread = Thread.currentThread();
        final long threadID = cThread.getId();
        ConWrapper ret = connections.get(threadID);

        if (ret == null) {
            Connection retCon = connectToDB();
            ret = new ConWrapper(retCon, threadID);

            synchronized (mutex) {
                connections.put(threadID, ret);
            }
        }
        return ret.getConnection();
    }

    public static final Connection connectToDB() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");    // touch the MSSQL driver
            //Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            Connection con = DriverManager.getConnection(DecryptedDatabaseStrings, DecryptedDatabaseStrings_u, DecryptedDatabaseStrings_p);

            if (connectionTimeOut == (5 * 60 * 1000)) {
                long timeout = getWaitTimeout(con);
                if (timeout != -1) {
                    connectionTimeOut = timeout;
                }
            }
            return con;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final void closeAll() throws SQLException {
        synchronized (mutex) {
            for (final ConWrapper con : connections.values()) {
                con.connection.close();
            }
            connections.clear();
        }
    }

    private static long getWaitTimeout(final Connection con) {
        if (TIMEOUT_CACHE != -1) {
            return TIMEOUT_CACHE;
        }
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SHOW VARIABLES LIKE 'wait_timeout'"); // TODO: This may be only for MySQL
            if (rs.next()) {
                TIMEOUT_CACHE = Math.max(1000, rs.getInt(2) * 1000 - 1000);
                return TIMEOUT_CACHE;
            } else {
                return -1;
            }
        } catch (SQLException ex) {
            return -1;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                }
            }
        }
    }

    private static class ClearingTask implements Runnable {

        @Override
        public void run() {
            final long cTime = System.currentTimeMillis();

            final Map<Long, ConWrapper> connections_cpy = new HashMap(connections);
            final List<Long> toRemove_con = new ArrayList();

            for (Map.Entry<Long, ConWrapper> con : connections_cpy.entrySet()) {
                if (con.getValue().closeIfexpiredConnection(cTime)) {
                    con.getValue().connection = null;
                    toRemove_con.add(con.getKey());
                }
            }
            synchronized (mutex) {
                for (Long s : toRemove_con) {
                    connections.remove(s);
                }
            }
            connections_cpy.clear();
        }
    }

    private static class ConWrapper {

        private long lastAccessTime = 0;
        private final long threadid;
        private Connection connection;

        public ConWrapper(Connection con, long threadid) {
            this.connection = con;
            this.threadid = threadid;
        }

        public final Connection getConnection() {
            if (closeIfexpiredConnection(System.currentTimeMillis())) {
                synchronized (mutex) {
                    connections.remove(threadid);
                }
                this.connection = connectToDB();
            }
            lastAccessTime = System.currentTimeMillis(); // Record Access
            return this.connection;
        }

        /**
         * Returns whether this connection has expired
         *
         * @return
         */
        public final boolean closeIfexpiredConnection(final long cTime) {
            if (lastAccessTime == 0) {
                return false;
            }
            try {
                if (cTime - lastAccessTime >= connectionTimeOut || connection.isClosed()) {
                    connection.close();
                    return true;
                }
            } catch (Throwable ex) { // couldnt close :(
                return true;
            }
            return false;
        }
    }
}
