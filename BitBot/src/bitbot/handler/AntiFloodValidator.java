package bitbot.handler;

import bitbot.util.FileoutputUtil;
import bitbot.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author z
 */
public class AntiFlood {
    
    // Anti flood
    private static final int AllowedInterval = 100,
            AllowedResetInveral = 20000;
    private static final String BlockedIPFileList = "BlockedIPs.properties";
    
    // variables
    private static final List<String> blacklist = new ArrayList();
    private static final Map<String, Pair<Long, Integer>> tracker = new HashMap();
    private static final Object mutex = new Object();
    
    static {
        File f = new File(BlockedIPFileList);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ioe) {
                System.out.println(ioe.toString() + " Error accessing file system to create Blocked IP list.");
            }
        }
        try (FileReader Reader = new FileReader(BlockedIPFileList)) {
            try (BufferedReader bufReader = new BufferedReader(Reader)) {
                String str;
                while ((str = bufReader.readLine()) != null) { // Read from the underlying StringReader.
                    block(str, false);
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString() + " Error accessing Blocked IP list.");
        }
    }
    
    public static boolean CheckSpam(String address) {
	if (isBlocked(address)) {
//	    if (Randomizer.nextInt(200) == 1) {	
//		FileoutputUtil.log("DOS_Log.rtf", String.format("DOS activity with : %s : %s", addr.toString(), saaddr.getHostName()));
//	    }
	   // session.close(true);
	    return true;
	}
	final Pair<Long, Integer> track = tracker.get(address);

	int count;
	long cTime = System.currentTimeMillis();
	if (track == null) {
	    synchronized (mutex) {
		tracker.put(address, new Pair(cTime, 1)); // 1 count
	    }
	} else {
	    count = track.right;

	    cTime = System.currentTimeMillis();
	    final long difference = cTime - track.left;
	    if (difference < AllowedInterval) {
		count++;
	    } else if (difference > AllowedResetInveral) {
		count = 1;
            }
	    if (count >= 10) {
		synchronized (mutex) {
		    block(address, true);
		}
		FileoutputUtil.log("DOS_Log.rtf", String.format("Banned IP address : %s", address));
		return true;
	    }
	    track.left = cTime == -1 ? System.currentTimeMillis() : cTime;
	    track.right = count;
	}
        return false;
    }
    
    public static boolean isBlocked(String address) {
        return blacklist.contains(address);
    }
    
    public static void block(String address, boolean removeRef) {
	synchronized (mutex) {
	    if (!blacklist.contains(address)) { // in an event that its spammed..
		blacklist.add(address);
	    } else {
		return; // already blacklisted
	    }
	    if (removeRef) {
		tracker.remove(address); // Cleanup
	    } else {
		return; // don't write to file or null router
	    }
	}
        system_blockIP(address);
    }
    
    private static void system_blockIP(String address) {
	// now we want to block this IP if it've ever spam once
	try (FileWriter Writer = new FileWriter(BlockedIPFileList, true)) {
	    Writer.append(address);
	    Writer.append(System.getProperty("line.separator"));// windows,mac,linux
            
            Writer.close();
	} catch (Exception e) {
        }
        
	if (System.getProperty("os.name").toLowerCase().contains("linux")) {
	    try {
                // gotcha! bye, you noobs.
		Runtime.getRuntime().exec(String.format("iptables --append INPUT --source %s -j DROP", address));
		Runtime.getRuntime().exec(String.format("route add %s gw 127.0.0.1 lo", address));
                
                // http://www.softpanorama.org/Net/Netutils/route_in_linux.shtml
	    } catch (IOException e) {
		FileoutputUtil.outputFileError("Error_Auto_null_route.rtf", e);
	    }
	} // TODO : Windows and other OS, but its unnecessary yet...
    }

    public static void unblock(InetAddress address) {
	synchronized (mutex) {
	    blacklist.remove(address.getHostAddress());
	}
    }
}
