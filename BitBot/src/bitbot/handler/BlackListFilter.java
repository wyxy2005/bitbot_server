package bitbot.handler;

import bitbot.util.FileoutputUtil;
import bitbot.util.Pair;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;
import org.apache.mina.filter.firewall.Subnet;

/**
 *
 * @author z
 */
public class BlackListFilter extends IoFilterAdapter {

    private static final int AllowedInterval = 1200,
            AllowedResetInveral = 20000;
    private static final String BlockedIPFileList = "BlockedIPs.properties";
    private final List<Subnet> blacklist = new ArrayList();
    private final Map<InetAddress, Pair<Long, Integer>> tracker = new HashMap();
    private final Object mutex = new Object();

    public BlackListFilter() {
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
                    block(InetAddress.getByName(str), false);
                }
            }
        } catch (IOException e) {
            System.out.println(e.toString() + " Error accessing Blocked IP list.");
        }
    }

    public void block(InetAddress address, boolean removeRef) {
        final Subnet sub = new Subnet(address, 32);
        synchronized (mutex) {
            if (!blacklist.contains(sub)) { // in an event that its spammed..
                this.blacklist.add(sub);
            } else {
                return; // already blacklisted
            }
            if (removeRef) {
                tracker.remove(address); // Cleanup
            } else {
                return; // don't write to file or null router
            }
        }
        String addr = address.toString();
        addr = addr.substring(1, addr.length());

        // now we want to block this IP if it've ever spam once
        try (FileWriter Writer = new FileWriter(BlockedIPFileList, true)) {
            Writer.append(addr);
            Writer.append("\r\n");
        } catch (Exception e) {
        }
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                Runtime.getRuntime().exec(String.format("iptables --append INPUT --source %s -j DROP", addr));
                Runtime.getRuntime().exec(String.format("route add %s gw 127.0.0.1 lo", addr));
            } catch (Exception e) {
                FileoutputUtil.outputFileError("Error_Auto_null_route.rtf", e);
            }
        }// TODO : Windows and other OS, but its unnecessary for us yet
    }

    public void unblock(InetAddress address) {
        synchronized (mutex) {
            this.blacklist.remove(new Subnet(address, 32));
        }
    }

    @Override
    public void sessionCreated(IoFilter.NextFilter nextFilter, IoSession session) {
        InetSocketAddress saaddr = (InetSocketAddress) session.getRemoteAddress();
        final InetAddress addr = saaddr.getAddress();

        if (isBlocked(addr)) {
//	    if (Randomizer.nextInt(200) == 1) {	
//		FileoutputUtil.log("DOS_Log.rtf", String.format("DOS activity with : %s : %s", addr.toString(), saaddr.getHostName()));
//	    }
            session.close(true);
            return;
        }
        final Pair<Long, Integer> track = tracker.get(addr);

        int count;
        long cTime = System.currentTimeMillis();;
        if (track == null) {
            synchronized (mutex) {
                tracker.put(addr, new Pair(cTime, 1)); // 1 count
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
                    block(addr, true);
                }
                session.close(true);
                FileoutputUtil.log("DOS_Log.rtf", String.format("Banned IP address : %s", addr.toString()));
                return;
            }
            track.left = cTime == -1 ? System.currentTimeMillis() : cTime;
            track.right = count;
        }
        nextFilter.sessionCreated(session);
    }

    @Override
    public void sessionOpened(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        InetSocketAddress saaddr = (InetSocketAddress) session.getRemoteAddress();
        final InetAddress addr = saaddr.getAddress();

        if (!isBlocked(addr)) {
            nextFilter.sessionOpened(session);
        } else {
            session.close(true);
        }
    }

    @Override
    public void sessionClosed(IoFilter.NextFilter nextFilter, IoSession session) throws Exception {
        nextFilter.sessionClosed(session);
    }

    @Override
    public void sessionIdle(IoFilter.NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        nextFilter.sessionIdle(session, status);
    }

    @Override
    public void messageReceived(IoFilter.NextFilter nextFilter, IoSession session, Object message) {
        nextFilter.messageReceived(session, message);
    }

    @Override
    public void messageSent(IoFilter.NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        nextFilter.messageSent(session, writeRequest);
    }

    private boolean isBlocked(InetAddress address) {
        for (Subnet subnet : blacklist) {
            if (subnet.inSubnet(address)) {
                return true;
            }
        }
        return false;
    }

}
