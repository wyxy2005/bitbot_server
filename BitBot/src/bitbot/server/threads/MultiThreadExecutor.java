package bitbot.server.threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/*
 * To be use mainly for high performance computing (eg : loading WZ?)
 */

public class MultiThreadExecutor {

    private static ExecutorService es;

    public static void start() {
	if (es != null && !es.isShutdown() && !es.isTerminated()) {
	    return; //starting the same timermanager twice is no - op
	}
	final ThreadFactory thread = new ThreadFactory() {

	    @Override
	    public Thread newThread(Runnable r) {
		final Thread t = new Thread(r);
		t.setName("MultiThreadExecutor worker");
		return t;
	    }
	};
	final ExecutorService es_ = Executors.newFixedThreadPool(100, thread);
	es = es_;
    }

    public static void stop() {
	es.shutdown();
    }

    public static Future<?> submit(Runnable r) {
	return es.submit(r);
    }
}