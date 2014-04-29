package bitbot.handler;

import bitbot.util.FileoutputUtil;
import bitbot.util.Pair;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class NonceValidator {
    
    // variables
    private static final Map<String, List<Long>> tracker = new HashMap(); // IP address paired with the list of nonces
    private static final ReentrantLock mutex = new ReentrantLock();
    
    public static boolean CheckSpam(String address, long Nonce) {
	final List<Long> track = tracker.get(address);

	long lastNonce = 0;
	if (track == null) {
            List<Long> trackerArray = new ArrayList<>();
            trackerArray.add(Nonce);
            
            mutex.lock();
            try {
		tracker.put(address, trackerArray); // 1 count
	    } finally {
                mutex.unlock();
            }
	} else {
	    lastNonce = track.get(track.size() - 1);

            // Check if the last nonce is smaller than the previous, or 0
            if (Nonce <= 0 || Nonce < lastNonce) {
                return false;
            }
            track.add(Nonce);
	}
        return true;
    }
    
}
