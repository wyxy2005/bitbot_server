package bitbot.util.database;

import bitbot.util.encryption.Base64;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class DatabaseConnection {

    private static String DatabaseConnectionString
           = "jdbc:sqlserver://fjoynp4lnu.database.secure.windows.net,1433;database=BitCoinATpqACXAu;encrypt=true;hostNameInCertificate=*.database.secure.windows.net;loginTimeout=300;socketTimeout=60;";
    private static String DecryptedDatabaseStrings_u = null;
    private static String DecryptedDatabaseStrings_p = null;

    private static final long connectionTimeOut_Minutes = 3; // 3 minutes
    private static final ReentrantLock mutex = new ReentrantLock();
    private static final Map<Long, ConWrapper> connections = new HashMap();

    static {
        String encrypted_u = "c2ZfMzRtZG9sc3NhZDk0NW0=";
        String encrypted_p = "MzEyZGZkNGY1OjpAJGRnZ2dmZzQ1NDU1NDY=";

        //System.out.println(Base64.encodeBytes("jdbc:sqlserver://fjoynp4lnu.database.windows.net:1433;database=BitCoinATpqACXAu;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=300;socketTimeout=60;".getBytes()));
        //DecryptedDatabaseStrings = "jdbc:jtds:sqlserver://fjoynp4lnu.database.windows.net:1433;DatabaseName=BitCoinATpqACXAu;useNTLMv2=true;useJCIFS=true;tcpNoDelay=true;instance=SQLEXPRESS;ssl=require";
        //DecryptedDatabaseStrings = "jdbc:sqlserver://fjoynp4lnu.database.windows.net:1433;database=BitCoinATpqACXAu;encrypt=true;hostNameInCertificate=*.database.windows.net;loginTimeout=300;";
        //System.out.println(Base64.encodeBytes(DecryptedDatabaseStrings.getBytes()));
        try {
            DecryptedDatabaseStrings_u = new String(Base64.decode(encrypted_u));
            DecryptedDatabaseStrings_p = new String(Base64.decode(encrypted_p));
        } catch (IOException exp) {
        }
    }

    public static final Connection getConnection() {
        // clearing task
        new Thread(new ClearingTask()).start();

        final long threadID = Thread.currentThread().getId();
        ConWrapper ret = connections.get(threadID);

        if (ret != null) {
            if (ret.closeIfexpiredConnection()) {
                ret = null;
            }
        }
        if (ret == null) {
            ret = new ConWrapper(threadID);

            mutex.lock();
            try {
                connections.put(threadID, ret);
            } finally {
                mutex.unlock();
            }
        }
        return ret.getConnection();
    }

    public static final void closeAll() throws SQLException {
        mutex.lock();
        try {
            for (final ConWrapper con : connections.values()) {
                con.connection.close();
                con.expiredConnection = true;
            }
            connections.clear();
        } finally {
            mutex.unlock();
        }
    }

    private static class ClearingTask implements Runnable {

        @Override
        public void run() {
            final Map<Long, ConWrapper> connections_cpy = new HashMap(connections);
            final List<Long> toRemove_con = new ArrayList();

            connections_cpy.entrySet().stream().filter((con) -> (con.getValue().isExpiredConnection())).map((con) -> {
                con.getValue().connection = null;
                return con;
            }).forEach((con) -> {
                toRemove_con.add(con.getKey());
            });
            mutex.lock();
            try {
                for (Long s : toRemove_con) {
                    connections.remove(s);
                }
            } finally {
                mutex.unlock();
            }
            connections_cpy.clear();
        }
    }

    private static class ConWrapper {

        private long lastAccessTime = 0;
        private final long threadid;
        private Connection connection;
        private boolean expiredConnection;

        public ConWrapper(long threadid) {
            this.connection = connectToDBInternal();
            this.threadid = threadid;
            this.expiredConnection = false;
        }

        public final Connection getConnection() {
            if (closeIfexpiredConnection()) {
                mutex.lock();
                try {
                    connections.remove(threadid);
                } finally {
                    mutex.unlock();
                }
                this.connection = connectToDBInternal();
                this.expiredConnection = false;
            }
            lastAccessTime = System.currentTimeMillis(); // Record Access
            return this.connection;
        }

        /**
         * Returns whether this connection has expired
         *
         * @return
         */
        private boolean closeIfexpiredConnection() {
            if (expiredConnection) {
                return true;
            }
            if (lastAccessTime == 0) {
                return false;
            }
            final long cTime = System.currentTimeMillis();

            try {
                if (cTime - lastAccessTime >= (connectionTimeOut_Minutes * 60 * 1000) || connection.isClosed()) {
                    expiredConnection = true;
                    connection.close();
                    return true;
                }
            } catch (Throwable ex) { // couldnt close :(
                expiredConnection = true;
                return true;
            }
            return false;
        }

        public boolean isExpiredConnection() {
            return expiredConnection;
        }

        private static Connection connectToDBInternal() {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                //Class.forName("net.sourceforge.jtds.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }

            try {
                Connection con = DriverManager.getConnection(DatabaseConnectionString, DecryptedDatabaseStrings_u, DecryptedDatabaseStrings_p);
                return con;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
