package bitbot.cache.swaps;

import bitbot.cache.tickers.HistoryDatabaseCommitEnum;
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
public class BacklogCommitTask_Swaps {
    
    private static final List<SwapsHistoryData> Cache_Backlog = new ArrayList();
    private static final Lock mutex = new ReentrantLock();
    private static LoggingSaveRunnable schedule = null; // find something to do with this reference

    private static final List<SwapsHistoryData> Cache_ImmediateBacklog = new ArrayList();
    private static final Lock ImmediateMutex = new ReentrantLock();
    private static LoggingSaveRunnable ImmediateSchedule = null; // find something to do with this reference

    static {
        System.out.println("Loading BacklogCommitTask_Swaps :::");

        schedule = TimerManager.register(new BacklogTimerTask_Swaps(), 1000 * 60 * 5, Integer.MAX_VALUE); // 5 minutes

        ImmediateSchedule = TimerManager.register(new ImmediateBacklogTimerTask_Swaps(), 5000, Integer.MAX_VALUE); // 1 second
    }

    public static final void RegisterForLogging(SwapsHistoryData type) {
        mutex.lock();
        try {
            Cache_Backlog.add(type);
            //System.out.println("Backlog size: " + Cache_Backlog.size());
        } finally {
            mutex.unlock();
        }
    }

    public static final void RegisterForLogging(List<SwapsHistoryData> data) {
        mutex.lock();
        try {
            Cache_Backlog.addAll(data);
        } finally {
            mutex.unlock();
        }
    }

    private static class BacklogTimerTask_Swaps implements Runnable {

        @Override
        public void run() {
            // Execute this in executor's thread, to prevent clogging of timer
            Thread t = new Thread(new BacklogTimerPersistingTask_Swaps());
            t.setPriority(3);// 1 = min, 10 = max;

            MultiThreadExecutor.submit(t);
        }
    }

    private static class BacklogTimerPersistingTask_Swaps implements Runnable {

        @Override
        public void run() {
            BacklogTimerPersistingTask();
        }
    }

    public static void BacklogTimerPersistingTask() {
        final List<SwapsHistoryData> DatabaseCommitBacklog_Copy = new ArrayList<>();
        final List<SwapsHistoryData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        mutex.lock();
        try {
            DatabaseCommitBacklog_Copy.addAll(Cache_Backlog);

            Cache_Backlog.clear();
        } finally {
            mutex.unlock();
        }
        for (SwapsHistoryData backlog : DatabaseCommitBacklog_Copy) {
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

    public static final void RegisterForImmediateLogging(SwapsHistoryData type) {
        ImmediateMutex.lock();
        try {
            Cache_ImmediateBacklog.add(type);
        } finally {
            ImmediateMutex.unlock();
        }
    }

    private static class ImmediateBacklogTimerTask_Swaps implements Runnable {

        @Override
        public void run() {
            // Execute this in executor's thread, to prevent clogging of timer
            Thread t = new Thread(new ImmediateBacklogTimerPersistingTask_Swaps());
            t.setPriority(7);// 1 = min, 10 = max;

            MultiThreadExecutor.submit(t);
        }
    }

    private static class ImmediateBacklogTimerPersistingTask_Swaps implements Runnable {

        @Override
        public void run() {
            ImmediateBacklogTimerPersistingTask();
        }
    }

    public static void ImmediateBacklogTimerPersistingTask() {
        final List<SwapsHistoryData> DatabaseCommitBacklog_Copy = new ArrayList<>();
        final List<SwapsHistoryData> DatabaseCommitBacklog_Failed = new ArrayList<>(); // Create a new array off existing data

        ImmediateMutex.lock();
        try {
            DatabaseCommitBacklog_Copy.addAll(Cache_ImmediateBacklog);

            Cache_ImmediateBacklog.clear();
        } finally {
            ImmediateMutex.unlock();
        }

        // commit to database in non locking condition
        Iterator<SwapsHistoryData> backlogItr = DatabaseCommitBacklog_Copy.iterator();
        while (backlogItr.hasNext()) {
            SwapsHistoryData backlog = backlogItr.next();

            HistoryDatabaseCommitEnum commitResult = backlog.commitDatabase();

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
