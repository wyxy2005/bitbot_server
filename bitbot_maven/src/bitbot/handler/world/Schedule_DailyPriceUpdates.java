package bitbot.handler.world;

import bitbot.push.PeriodicPushNotification;
import bitbot.server.threads.TimerManager;
import java.util.Calendar;

/**
 *
 * @author zhenghao
 */
public class Schedule_DailyPriceUpdates {

    private static Calendar CurrentRun = null;

    static {
        System.out.println("Starting daily periodic daily price notification :::");

        calculateWeighing();
    }

    public static void start() {
        // dummy method.
    }

    public static final void calculateWeighing() {
        final long cTime = System.currentTimeMillis();
        CurrentRun = Calendar.getInstance();

        CurrentRun.set(Calendar.HOUR_OF_DAY, 0);
        CurrentRun.set(Calendar.MINUTE, 0);
        CurrentRun.set(Calendar.SECOND, 0);
        CurrentRun.set(Calendar.MILLISECOND, 0);

        if (cTime > CurrentRun.getTimeInMillis()) { // but..  over the time
            CurrentRun.add(Calendar.DATE, 1);
        }

        long sch = Math.max(1, CurrentRun.getTimeInMillis() - cTime);
        System.out.println(" Scheduling periodic daily price notification in [" + sch / 1000 + " seconds]" + (sch / 1000 / 60 / 60) + " hours");

        TimerManager.scheduleAtTimestamp(new ScheduleStartEvent(), CurrentRun.getTimeInMillis());
        //TimerManager.scheduleAtTimestamp(new ScheduleStartEvent(), CurrentRun.getTimeInMillis());
    }

    private static class ScheduleStartEvent implements Runnable {

        @Override
        public void run() {
            try {
                PeriodicPushNotification.getInstance().sendDailyPushNotification();
            } finally {
                calculateWeighing(); // recalculate
            }
        }
    }
}
