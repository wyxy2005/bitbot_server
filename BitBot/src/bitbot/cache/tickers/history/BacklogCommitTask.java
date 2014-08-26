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

        ImmediateSchedule = TimerManager.register(new ImmediateBacklogTimerTask(), 5000, Integer.MAX_VALUE); // 1 second
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
        final List<TickerHistoryData> DatabaseCommitBacklog_Copy = new ArrayList<>();
        final List<TickerHistoryData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        mutex.lock();
        try {
            DatabaseCommitBacklog_Copy.addAll(Cache_Backlog);

            Cache_Backlog.clear();
        } finally {
            mutex.unlock();
        }
        for (TickerHistoryData backlog : DatabaseCommitBacklog_Copy) {
            HistoryDatabaseCommitEnum commitResult = backlog.commitDatabase(
                    0, // dummy values
                    null, null);

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
        final List<TickerHistoryData> DatabaseCommitBacklog_Copy = new ArrayList<>();
        final List<TickerHistoryData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        ImmediateMutex.lock();
        try {
            DatabaseCommitBacklog_Copy.addAll(Cache_ImmediateBacklog);

            Cache_ImmediateBacklog.clear();
        } finally {
            ImmediateMutex.unlock();
        }

        // commit to database in non locking condition
        Iterator<TickerHistoryData> backlogItr = DatabaseCommitBacklog_Copy.iterator();
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

        // Register the failed items to the main backlog list that'll commit once every 5 minutes instead
        // This line must always be after the ImmediateMutex.unlock to avoid deadlocks
        if (!DatabaseCommitBacklog_Failed.isEmpty()) {
            ImmediateMutex.lock();
            try {
                Cache_ImmediateBacklog.addAll(DatabaseCommitBacklog_Failed);
            } finally {
                ImmediateMutex.unlock();
            }
        }
        // GC
        DatabaseCommitBacklog_Failed.clear();
        DatabaseCommitBacklog_Copy.clear();
    }
}
