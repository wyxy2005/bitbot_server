package bitbot.server.threads;

import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ThreadPool {

    private static ThreadPool instance = new ThreadPool();
    private ThreadPoolExecutor ses;
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue();

    public static ThreadPool getInstance() {
	return instance;
    }

    public void start() {
	if (ses != null && !ses.isShutdown() && !ses.isTerminated()) {
	    return; //starting the same timermanager twice is no - op
	}
	final ThreadFactory thread = new ThreadFactory() {

	    @Override
	    public Thread newThread(Runnable r) {
		final Thread t = new Thread(r);
		t.setName("ThreadExecutor worker");
		return t;
	    }
	};
	final ThreadPoolExecutor stpe = new ThreadPoolExecutor(
		10, // min core
		20, // max core
		50, TimeUnit.SECONDS, // Timeout delay
		queue, thread);
	stpe.allowCoreThreadTimeOut(true);
	ses = stpe;
    }

    public void stop() {
	ses.shutdown();
    }

    public void execute(Runnable r) {
	ses.execute(new LoggingSaveRunnable(r));
    }

    private class LoggingSaveRunnable implements Runnable {

	Runnable r;

	public LoggingSaveRunnable(final Runnable r) {
	    this.r = r;
	}

	@Override
	public void run() {
	    try {
		r.run();
	    } catch (Throwable t) {
		ServerLog.RegisterForLoggingException(ServerLogType.Timer, t);
	    }
	}
    }
}