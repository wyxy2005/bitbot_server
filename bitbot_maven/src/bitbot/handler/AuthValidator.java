package bitbot.handler;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

/**
 *http://oauth.googlecode.com/svn/code/java/core/provider/src/main/java/net/oauth/SimpleOAuthValidator.java
 * @author z
 */
public class AuthValidator {
    /** The default maximum age of timestamps is 5 minutes. */
    public static final long DEFAULT_MAX_TIMESTAMP_AGE = 5 * 60 * 1000L;
    public static final long DEFAULT_TIMESTAMP_WINDOW = DEFAULT_MAX_TIMESTAMP_AGE;
    
    protected final double minVersion = 1.0;
    protected final double maxVersion;
    protected final long maxTimestampAgeMsec;
    private final Set<UsedNonce> usedNonces = new TreeSet<>();
    
    /**
     * Construct a validator that rejects messages more than five minutes old or
     * with a OAuth version other than 1.0.
     */
    public AuthValidator() {
        this(DEFAULT_TIMESTAMP_WINDOW, 1);
    }
    
    /**
     * Public constructor.
     * 
     * @param maxTimestampAgeMsec
     *            the range of valid timestamps, in milliseconds into the past
     *            or future. So the total range of valid timestamps is twice
     *            this value, rounded to the nearest second.
     * @param maxVersion
     *            the maximum valid oauth_version
     */
    public AuthValidator(long maxTimestampAgeMsec, double maxVersion) {
        this.maxTimestampAgeMsec = maxTimestampAgeMsec;
        this.maxVersion = maxVersion;

    }
    
    /**
     * Remove usedNonces with timestamps that are too old to be valid.
     */
    private Date removeOldNonces(long currentTimeMsec) {
        UsedNonce next = null;
        /*UsedNonce min = new UsedNonce((currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L);
        synchronized (usedNonces) {
            // Because usedNonces is a TreeSet, its iterator produces
            // elements from oldest to newest (their natural order).
            for (Iterator<UsedNonce> iter = usedNonces.iterator(); iter.hasNext();) {
                UsedNonce used = iter.next();
                if (min.compareTo(used) <= 0) {
                    next = used;
                    break; // all the rest are also new enough
                }
                iter.remove(); // too old
            }
        }
        if (next == null)
            return null;*/
        return new Date((next.getTimestamp() * 1000L) + maxTimestampAgeMsec + 500);
    }
   
    /**
     * Throw an exception if the timestamp is out of range or the nonce has been
     * validated previously.
     * 
     * @param timestamp 
     * 
     * @return boolean
     */
    protected boolean validateTimestampAndNonce(long timestamp, long nonce) {
        long now = System.currentTimeMillis();
        
        return validateTimestamp(timestamp, now) && validateNonce(timestamp, nonce, now);
    }

    /** Throw an exception if the timestamp [sec] is out of range. 
     * 
     * @param timestamp
     * @param currentTimeMsec
     * 
     * @return boolean
     */
    private boolean validateTimestamp(long timestamp, long currentTimeMsec)  {
        long min = (currentTimeMsec - maxTimestampAgeMsec + 500) / 1000L;
        long max = (currentTimeMsec + maxTimestampAgeMsec + 500) / 1000L;
        
        if (timestamp < min || max < timestamp) {
            return false;
        }
        return true;
    }

    /**
     * Throw an exception if the nonce has been validated previously.
     * 
     * @param timestamp
     * @param inputnonce
     * @param currentTimeMsec
     * 
     * @return the earliest point in time at which a call to releaseGarbage
     *         will actually release some garbage, or null to indicate there's
     *         nothing currently stored that will become garbage in future.
     */
    private boolean validateNonce(long timestamp, long inputnonce, long currentTimeMsec) {
        UsedNonce nonce = new UsedNonce(timestamp, inputnonce);
        /*
         * The OAuth standard requires the token to be omitted from the stored
         * nonce. But I include it, to harmonize with a Consumer that generates
         * nonces using several independent computers, each with its own token.
         */
        boolean valid = false;
        synchronized (usedNonces) {
            valid = usedNonces.add(nonce);
        }
        if (!valid) {
            return false;
        }
        removeOldNonces(currentTimeMsec);
        return true;
    }
    
    /**
     * Selected parameters from an OAuth request, in a form suitable for
     * detecting duplicate requests. The implementation is optimized for the
     * comparison operations (compareTo, equals and hashCode).
     * 
     * @author John Kristian
     */
    private static class UsedNonce implements Comparable<UsedNonce> {
        /**
         * Construct an object containing the given timestamp, nonce and other
         * parameters. The order of parameters is significant.
         */
        public UsedNonce(long timestamp, long nonce) {
            sortKey = "";//timestamp.toString();
        }

        private final String sortKey;

        long getTimestamp() {
            int end = sortKey.indexOf("&");
            if (end < 0)
                end = sortKey.length();
            return Long.parseLong(sortKey.substring(0, end).trim());
        }

        /**
         * Determine the relative order of <code>this</code> and
         * <code>that</code>, as specified by Comparable. The timestamp is most
         * significant; that is, if the timestamps are different, return 1 or
         * -1. If <code>this</code> contains only a timestamp (with no nonce
         * etc.), return -1 or 0. The treatment of the nonce etc. is murky,
         * although 0 is returned only if they're all equal.
         */
        public int compareTo(UsedNonce that) {
            return (that == null) ? 1 : sortKey.compareTo(that.sortKey);
        }

        @Override
        public int hashCode() {
            return sortKey.hashCode();
        }

        /**
         * Return true iff <code>this</code> and <code>that</code> contain equal
         * timestamps, nonce etc., in the same order.
         */
        @Override
        public boolean equals(Object that) {
            if (that == null)
                return false;
            if (that == this)
                return true;
            if (that.getClass() != getClass())
                return false;
            return sortKey.equals(((UsedNonce) that).sortKey);
        }

        @Override
        public String toString() {
            return sortKey;
        }
    }

}
