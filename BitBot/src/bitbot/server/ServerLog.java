/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitbot.server;

import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.MultiThreadExecutor;
import bitbot.server.threads.TimerManager;
import bitbot.util.FileoutputUtil;
import bitbot.util.Pair;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class ServerLog {
    
    private static final List<Pair<ServerLogType, String>> CacheLogMsg = new ArrayList();
    private static final List<Pair<ServerLogType, Throwable>> CacheLogException = new ArrayList();
    private static final Lock mutex = new ReentrantLock();
    private static LoggingSaveRunnable schedule = null; // find something to do with this reference
    
    static {
	System.out.println("Loading ServerLog :::");

	schedule = TimerManager.register(new TimerTask(), 1000 * 15, Integer.MAX_VALUE); // 15 secs
    }

    public static void load() {
	// empty
    }

    public static final void RegisterForLogging(ServerLogType type, String msg) {
	mutex.lock();
	try {
	    CacheLogMsg.add(new Pair(type, msg));
	} finally {
	    mutex.unlock();
	}
    }

    public static final void RegisterForLoggingException(ServerLogType type, Throwable t) {
	mutex.lock();
	try {
	    CacheLogException.add(new Pair(type, t));
	} finally {
	    mutex.unlock();
	}
    }

    private static String getFileTimeString(String file) {
	final Calendar cal = Calendar.getInstance();
	return String.format("%s;%d.%d.%d.rtf", file, cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }


    public static void FlushLogs() {
	final Map<ServerLogType, List<String>> CacheLogMsg_Copy = new HashMap();
	final Map<ServerLogType, List<Throwable>> CacheLogException_Copy = new HashMap();

	mutex.lock();
	try {
	    // Don't lock this huge I/O thread, do it outside..
	    for (Pair<ServerLogType, String> s : CacheLogMsg) {
		List<String> get = CacheLogMsg_Copy.get(s.left);
		if (get == null) {
		    get = new ArrayList();
		    CacheLogMsg_Copy.put(s.left, get);
		}
		get.add(s.right);
	    }
	    for (Pair<ServerLogType, Throwable> s : CacheLogException) {
		List<Throwable> get = CacheLogException_Copy.get(s.left);
		if (get == null) {
		    get = new ArrayList();
		    CacheLogException_Copy.put(s.left, get);
		}
		get.add(s.right);
	    }
	    CacheLogMsg.clear();
	    CacheLogException.clear();
	} finally {
	    mutex.unlock();
	}
	if (!CacheLogMsg_Copy.isEmpty()) {
	    for (Map.Entry<ServerLogType, List<String>> log : CacheLogMsg_Copy.entrySet()) {
		FileoutputUtil.logList(getFileTimeString(log.getKey().getcoutFile()), log.getKey().getLogGroup().name(), log.getValue());
	    }
	}
	if (!CacheLogException_Copy.isEmpty()) {
	    for (Map.Entry<ServerLogType, List<Throwable>> log : CacheLogException_Copy.entrySet()) {
		FileoutputUtil.outputFileErrorList(getFileTimeString(log.getKey().getcoutFile()), log.getKey().getLogGroup().name(), log.getValue());
	    }
	}
	CacheLogMsg_Copy.clear();
	CacheLogException_Copy.clear();
    }

    private static class TimerTask implements Runnable {

	@Override
	public void run() {
	    // Execute this in executor's thread, to prevent clogging of timer
	    Thread t = new Thread(new LogPersistingTask());
	    t.setPriority(3);// 1 = min, 10 = max;

	    MultiThreadExecutor.submit(t);
	}
    }

    private static class LogPersistingTask implements Runnable {

	@Override
	public void run() {
	    FlushLogs();
	}
    }
}
