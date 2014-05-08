package bitbot.cache.tickers.history;

import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
import java.util.Iterator;
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

    private static final List<TickerHistoryData> Cache_ImmediateBacklog = new ArrayList();
    private static final Lock ImmediateMutex = new ReentrantLock();
    private static LoggingSaveRunnable ImmediateSchedule = null; // find something to do with this reference

    static {
        System.out.println("Loading HistoryBacklog :::");

        schedule = TimerManager.register(new BacklogTimerTask(), 1000 * 60 * 5, Integer.MAX_VALUE); // 5 minutes

        ImmediateSchedule = TimerManager.register(new ImmediateBacklogTimerTask(), 1000, Integer.MAX_VALUE); // 1 second
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

    public static final void RegisterForLogging(List<TickerHistoryData> data) {
        mutex.lock();
        try {
            Cache_Backlog.addAll(data);
            System.out.println("Backlog size2: " + Cache_Backlog.size());
        } finally {
            mutex.unlock();
        }
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
            BacklogTimerPersistingTask();
        }
    }

    public static void BacklogTimerPersistingTask() {
        mutex.lock();
        try {
            List<TickerHistoryData> DatabaseCommitBacklog2 = new ArrayList<>(Cache_Backlog); // Create a new array off existing data

            Cache_Backlog.clear(); // Clear existing

            for (TickerHistoryData backlog : DatabaseCommitBacklog2) {
                HistoryDatabaseCommitEnum commitResult = backlog.commitDatabase(
                        0, // dummy values
                        null, null);

                if (commitResult != HistoryDatabaseCommitEnum.Ok) { // if failed once again, add back. Lulz.
                    Cache_Backlog.add(backlog);
                }
            }
            DatabaseCommitBacklog2.clear();
        } finally {
            mutex.unlock();
        }
    }

    public static final void RegisterForImmediateLogging(TickerHistoryData type) {
        ImmediateMutex.lock();
        try {
            Cache_ImmediateBacklog.add(type);
        } finally {
            ImmediateMutex.unlock();
        }
    }

    private static class ImmediateBacklogTimerTask implements Runnable {

        @Override
        public void run() {
            // Execute this in executor's thread, to prevent clogging of timer
            Thread t = new Thread(new ImmediateBacklogTimerPersistingTask());
            t.setPriority(7);// 1 = min, 10 = max;

            MultiThreadExecutor.submit(t);
        }
    }

    private static class ImmediateBacklogTimerPersistingTask implements Runnable {

        @Override
        public void run() {
            ImmediateBacklogTimerPersistingTask();
        }
    }

    public static void ImmediateBacklogTimerPersistingTask() {
        List<TickerHistoryData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        ImmediateMutex.lock();
        try {
            Iterator<TickerHistoryData> backlogItr = Cache_ImmediateBacklog.iterator();
            while (backlogItr.hasNext()) {
                TickerHistoryData backlog = backlogItr.next();

                HistoryDatabaseCommitEnum commitResult = backlog.commitDatabase(
                        0, // dummy values
                        null, null);

                if (commitResult != HistoryDatabaseCommitEnum.Ok) { // if failed once again, add back. Lulz.
                    DatabaseCommitBacklog_Failed.add(backlog);
                } else {
                    backlogItr.remove(); // remove list if successful
                }
            }
        } finally {
            ImmediateMutex.unlock();
        }
            // Register the failed items to the main backlog list that'll commit once every 5 minutes instead
        // This line must always be after the ImmediateMutex.unlock to avoid deadlocks
        if (!DatabaseCommitBacklog_Failed.isEmpty()) {
            RegisterForLogging(DatabaseCommitBacklog_Failed);
        }
        // GC
        DatabaseCommitBacklog_Failed.clear();
    }
}
