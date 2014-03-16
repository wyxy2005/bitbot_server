package bitbot.server.threads;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * This is a dedicated timer to handle pings for character,
 */

public class TimerManager_Ping {

    private static ScheduledThreadPoolExecutor ses;

    public static void start() {
	if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
	    return; //starting the same timermanager twice is no - op
	}
	final ThreadFactory thread = new ThreadFactory() {

	    private final AtomicInteger threadNumber = new AtomicInteger(1);

	    @Override
	    public Thread newThread(Runnable r) {
		final Thread t = new Thread(r);
		t.setName("Timermanager-Worker-" + threadNumber.getAndIncrement());
		return t;
	    }
	};
	final ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(10, thread);
	stpe.setKeepAliveTime(1, TimeUnit.MINUTES);
	stpe.allowCoreThreadTimeOut(true);
	stpe.setCorePoolSize(10);
	stpe.setMaximumPoolSize(20);
	stpe.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
	stpe.setRemoveOnCancelPolicy(true);
	ses = stpe;
    }

    public static void stop() {
	ses.shutdown();
    }
    
    public static final int getActiveThreadCount() {
	return ses.getActiveCount();
    }
    
    public static final long getCompletedTaskCount() {
	return ses.getCompletedTaskCount();
    }

    public static LoggingSaveRunnable register(Runnable r, long repeatTime, long delay, int MaxAllowedExecutionCount) {
	final LoggingSaveRunnable runnable = new LoggingSaveRunnable(r, LogThreadType.Timer, MaxAllowedExecutionCount);
	
	final ScheduledFuture<?> schedule = ses.scheduleAtFixedRate(runnable, delay, repeatTime, TimeUnit.MILLISECONDS);
	runnable.setSchedule(schedule);
	
	return runnable;
    }

    public static LoggingSaveRunnable register(Runnable r, long repeatTime, int MaxAllowedExecutionCount) {
	final LoggingSaveRunnable runnable = new LoggingSaveRunnable(r, LogThreadType.Timer, MaxAllowedExecutionCount);
	
	final ScheduledFuture<?> schedule = ses.scheduleAtFixedRate(runnable, 0, repeatTime, TimeUnit.MILLISECONDS);
	runnable.setSchedule(schedule);
	
	return runnable;
    }

    public static LoggingSaveRunnable schedule(Runnable r, long delay) {
	final LoggingSaveRunnable runnable = new LoggingSaveRunnable(r, LogThreadType.Timer);
	
	final ScheduledFuture<?> schedule = ses.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	runnable.setSchedule(schedule);
	
	return runnable;
    }

    public static LoggingSaveRunnable scheduleAtTimestamp(Runnable r, long timestamp) {
	final LoggingSaveRunnable runnable = new LoggingSaveRunnable(r, LogThreadType.Timer);
	
	final ScheduledFuture<?> schedule = ses.schedule(runnable, timestamp - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	runnable.setSchedule(schedule);
	
	return runnable;
    }
}