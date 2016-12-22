package bitbot.server.threads;

import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
import java.util.concurrent.ScheduledFuture;

public class LoggingSaveRunnable implements Runnable {

    private Runnable r; // The underlying runnable task
    private LogThreadType type; // Enum to determine what type of timer executed it from
    private int ThreadRunningTimes = 0;
    private int MaxAllowedExecutionCount = 0;
    private Throwable te; // Debugging purpose.
    private ScheduledFuture<?> schedule; // May be used to cancel the task in between if should anything be invalid or error

    public LoggingSaveRunnable(final Runnable r, final LogThreadType type) {
	this.r = r;
	te = new Exception(); // Set where the thread originate from for debugging purpose.
	this.type = type;
	this.MaxAllowedExecutionCount = 10;
    }
    
    public LoggingSaveRunnable(final Runnable r, final LogThreadType type, final int MaxAllowedExecutionCount) {
	this.r = r;
	te = new Exception(); // Set where the thread originate from for debugging purpose.
	this.type = type;
	this.MaxAllowedExecutionCount = MaxAllowedExecutionCount;
    }

    public LogThreadType getLogThreadType() {
	return type;
    }
    
    public void setSchedule(ScheduledFuture future) {
	this.schedule = future;
    }
    
    public ScheduledFuture<?> getSchedule() {
	return schedule;
    }

    public Throwable getThreadOrigin() {
	return te;
    }

    public int getThreadRunningTimes() {
	return ThreadRunningTimes;
    }

    @Override
    public void run() {
	ThreadRunningTimes++;
	if (ThreadRunningTimes > MaxAllowedExecutionCount) { // might overrun, an uncancelled task, log to keep the developer aware of this
	    ServerLog.RegisterForLoggingException(ServerLogType.ThreadError, te);
	    this.getSchedule().cancel(false);
	    return;
	}
//	System.out.println("Map timer count : "+MapTimer.getActiveThreadCount()+" Timer Count : " + TimerManager.getActiveThreadCount() + " Ping : " + TimerManager_Ping.getActiveThreadCount());
	try {
	    r.run();
	} catch (Throwable t) {
	    switch (type) {
		case Map_Timer:
		    ServerLog.RegisterForLoggingException(ServerLogType.MapTimer, t);
		    break;
		case Ping_Timer:
		case Timer:
		    ServerLog.RegisterForLoggingException(ServerLogType.Timer, t);
		    break;
	    }
	}
    }
}
