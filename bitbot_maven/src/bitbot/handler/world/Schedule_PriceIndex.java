package bitbot.handler.world;

import bitbot.cache.tickers.index.TickerHistoryIndexCurrency;
import bitbot.cache.tickers.index.TickerHistoryIndexData;
import bitbot.server.threads.TimerManager;
import java.util.Calendar;

/**
 *
 * @author twili
 */
public class Schedule_PriceIndex {

    private static Calendar CurrentRun = null;

    static {
        System.out.println("Starting price index schedule :::");

        calculateWeighing();
    }

    public static void start() {
        // dummy method.
    }

    public static final void calculateWeighing() {
        final long cTime = System.currentTimeMillis();
        CurrentRun = Calendar.getInstance();

        CurrentRun.set(Calendar.MINUTE, 0);
        CurrentRun.set(Calendar.SECOND, 0);

        while (cTime > CurrentRun.getTimeInMillis()) { // but..  over the time
            CurrentRun.add(Calendar.MINUTE, 3);
        }

        long sch = Math.max(1, CurrentRun.getTimeInMillis() - cTime);
        System.out.println(" Calculating price index in [" + sch / 1000 + " seconds]" + (sch / 1000 / 60) + " minutes");

        TimerManager.scheduleAtTimestamp(new ScheduleStartEvent(), CurrentRun.getTimeInMillis());
        //TimerManager.scheduleAtTimestamp(new ScheduleStartEvent(), CurrentRun.getTimeInMillis());
    }

    private static class ScheduleStartEvent implements Runnable {

        @Override
        public void run() {
            try {

                final long cTime = System.currentTimeMillis();
                
                final double avgBTCUSDPrice = WorldChannelInterfaceImpl.getBitcoinIndexPrice(WorldChannelInterfaceImpl.USD_PAIRS_INDEX_LIST);
                if (avgBTCUSDPrice != 0) {
                    TickerHistoryIndexData indexPriceUSDData = new TickerHistoryIndexData(avgBTCUSDPrice, cTime,
                            TickerHistoryIndexCurrency.USDollar);
                    indexPriceUSDData.addToCommitBacklog();
                }
                final double avgBTCCNYPrice = WorldChannelInterfaceImpl.getBitcoinIndexPrice(WorldChannelInterfaceImpl.CNY_PAIRS_INDEX_LIST);
                if (avgBTCCNYPrice != 0) {
                    TickerHistoryIndexData indexPriceCNYData = new TickerHistoryIndexData(avgBTCUSDPrice, cTime,
                            TickerHistoryIndexCurrency.ChineseYuan);
                    indexPriceCNYData.addToCommitBacklog();
                }
               // System.out.println("Index price USD: " + avgBTCUSDPrice);
              //  System.out.println("Index Price CNY: " + avgBTCCNYPrice);
            } finally {
                calculateWeighing(); // recalculate
            }
        }
    }
}
