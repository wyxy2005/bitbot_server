package bitbot.cache.tickers;

import bitbot.cache.trades.TickerTradesData;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author zheng
 */
public class BacklogCommitTask_Trades {

    private static final List<TickerTradesData> Cache_Backlog = new ArrayList();
    private static final Lock mutex = new ReentrantLock();
    private static LoggingSaveRunnable schedule = null; // find something to do with this reference

    static {
        System.out.println("Loading BacklogCommitTask_Trades :::");

        schedule = TimerManager.register(new BacklogTimerTask_Trades(), 1000 * 60 * 1, Integer.MAX_VALUE); // 1 min
    }

    public static final void RegisterForLogging(TickerTradesData type) {
        mutex.lock();
        try {
            Cache_Backlog.add(type);
            System.out.println("Trades backlog size: " + Cache_Backlog.size());
        } finally {
            mutex.unlock();
        }
    }

    private static class BacklogTimerTask_Trades implements Runnable {

        @Override
        public void run() {
            // Execute this in executor's thread, to prevent clogging of timer
            Thread t = new Thread(new BacklogTimerPersistingTask_Trades());
            t.setPriority(4);// 1 = min, 10 = max;

            MultiThreadExecutor.submit(t);
        }
    }

    private static class BacklogTimerPersistingTask_Trades implements Runnable {

        @Override
        public void run() {
            BacklogTimerPersistingTask();
        }
    }

    public static void BacklogTimerPersistingTask() {
        final List<TickerTradesData> DatabaseCommitBacklog_Copy = new ArrayList<>();
        final List<TickerTradesData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        mutex.lock();
        try {
            DatabaseCommitBacklog_Copy.addAll(Cache_Backlog);

            Cache_Backlog.clear();
        } finally {
            mutex.unlock();
        }
        for (TickerTradesData backlog : DatabaseCommitBacklog_Copy) {
            HistoryDatabaseCommitEnum commitResult = backlog.commitDatabase();

            if (commitResult != HistoryDatabaseCommitEnum.Ok) { // if failed once again, add back. Lulz.
                DatabaseCommitBacklog_Failed.add(backlog);
            }
        }

        // add back if necessary
        if (!DatabaseCommitBacklog_Failed.isEmpty()) {
            mutex.lock();
            try {
                Cache_Backlog.addAll(DatabaseCommitBacklog_Failed);
            } finally {
                mutex.unlock();
            }
        }

        // GC
        DatabaseCommitBacklog_Copy.clear();
        DatabaseCommitBacklog_Failed.clear();
    }
}
