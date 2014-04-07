package bitbot.cache.tickers.history;

import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class BacklogCommitTask {

    private static final List<TickerHistoryData> Cache_Backlog = new ArrayList();
    private static final Lock mutex = new ReentrantLock();
    private static LoggingSaveRunnable schedule = null; // find something to do with this reference

    static {
        System.out.println("Loading HistoryBacklog :::");

        schedule = TimerManager.register(new BacklogTimerTask(), 1000 * 60 * 5, Integer.MAX_VALUE); // 5 minutes
    }

    public static final void RegisterForLogging(TickerHistoryData type) {
        mutex.lock();
        try {
            Cache_Backlog.add(type);
            System.out.println("Backlog size: " + Cache_Backlog.size());
        } finally {
            mutex.unlock();
        }
    }

    public static void FlushLogs() {
        List<TickerHistoryData> DatabaseCommitBacklog2 = new ArrayList<>(Cache_Backlog); // Create a new array off existing data
        
        mutex.lock();
        try {
            Cache_Backlog.clear(); // Clear existing
            
            for (TickerHistoryData backlog : DatabaseCommitBacklog2) {
                HistoryDatabaseCommitState commitResult = backlog.commitDatabase(
                        0, // dummy values
                        null, null);

                if (commitResult != HistoryDatabaseCommitState.Ok) { // if failed once again, add back. Lulz.
                    Cache_Backlog.add(backlog);
                }
            }
        } finally {
            mutex.unlock();
        }
        
        DatabaseCommitBacklog2.clear();
    }

    private static class BacklogTimerTask implements Runnable {

        @Override
        public void run() {
            // Execute this in executor's thread, to prevent clogging of timer
            Thread t = new Thread(new BacklogTimerPersistingTask());
            t.setPriority(3);// 1 = min, 10 = max;

            MultiThreadExecutor.submit(t);
        }
    }

    private static class BacklogTimerPersistingTask implements Runnable {

        @Override
        public void run() {
            FlushLogs();
        }
    }
}
