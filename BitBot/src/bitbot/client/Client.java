package bitbot.client;

import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import bitbot.util.encryption.AESOFB;
import bitbot.util.packets.LoginPacket;
import java.io.Serializable;
import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.mina.core.session.IoSession;

/**
 *
 * @author z
 */
public class Client implements Serializable {

    // Global
    private static final long serialVersionUID = 1993413738569L;

    // Session
    private volatile transient IoSession session;
    private volatile transient AESOFB send, receive;
    private final transient Lock mutex = new ReentrantLock(true);
    private transient long lastPong = 0, lastPing = 0;
    private transient int Latency = 500; // Default Latency
    private transient LoggingSaveRunnable idleTask = null;

    // Anticheats
    public boolean FlagPendingDisconnection = false; // Tells client that it've been hacking and DC within 15 seconds
    private volatile boolean SentPing = false;
    private volatile int pongcount = 0;
    public boolean SentCrashDump = false; // To prevent people from spamming crash dump and flood the server
    public long LastCapturedTimeMillis; // Current time is stored here everytime a packet is sent, mainly to prevent packet spam but its safe to use it for other operations 
    public long LastCapturedTimeMillis_500MSThreshold;
    public int PacketSpamCountWithinHalfSecond = 0;

    public Client(IoSession session) {
        this.session = session;
        this.LastCapturedTimeMillis = System.currentTimeMillis();
        this.LastCapturedTimeMillis_500MSThreshold = this.LastCapturedTimeMillis;
    }
    
    public Client(AESOFB send, AESOFB receive, IoSession session) {
        this.send = send;
        this.receive = receive;
        this.session = session;
        this.LastCapturedTimeMillis = System.currentTimeMillis();
        this.LastCapturedTimeMillis_500MSThreshold = this.LastCapturedTimeMillis;
    }

    public void disconnect() {

    }

    public final Lock getLock() {
        return mutex;
    }

    public void closeSession() {
        session.close(true);
    }

    public final AESOFB getReceiveCrypto() {
        return receive;
    }

    public final AESOFB getSendCrypto() {
        return send;
    }

    public void flagPendingDisconnection() {
        FlagPendingDisconnection = true;
    }

    public final int getLatency() {
        return this.Latency;
    }

    public final long getLastPong() {
        return lastPong;
    }

    public final LoggingSaveRunnable getIdleTask() {
        return idleTask;
    }

    public final void sendPing() {
        final long cTime = System.currentTimeMillis();
        if (cTime - lastPong > 30000 && lastPong != 0) { // 15 sec lag + 15 sec ping delay
            closeSession();
            return;
        }
        lastPing = cTime;
        SentPing = true;
        pongcount++;
        session.write(LoginPacket.getPing()); // sending Ping request
    }

    public final void sendPing_MinaSocketCore() {
        final long then = System.currentTimeMillis();
        pongcount++;
        session.write(LoginPacket.getPing()); // sending Ping request

        idleTask = TimerManager.schedule(() -> {
            try {
                if (lastPong - then < 0) {
                    if (session.isConnected()) {
                        closeSession();
                    }
                }
            } catch (NullPointerException e) {
                // client already gone
            }
        }, 15000); // note: idletime gets added to this too
    }

    public final void pongReceived(final boolean InvalidWindowTitle) {
        if (FlagPendingDisconnection) {
            this.closeSession();
            return;
        }
        pongcount--;
        if (pongcount < -10) {
            //AutobanManager.autoban(this, String.format("Abnormal pong rate of %d, normal value : 0", pongcount));
            closeSession();
            return;
        }
        final Calendar cal = Calendar.getInstance();
        this.Latency = (int) ((cal.getTimeInMillis() - lastPing) / 2);
        lastPong = cal.getTimeInMillis();
    }

}
