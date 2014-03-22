package bitbot.cache.tickers;

import bitbot.external.AzureBitBot;
import bitbot.graph.ExponentialMovingAverage;
import bitbot.graph.ExponentialMovingAverageData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class TickerCacheTask {

    private static final int BTCe_CacheRefreshTime_Seconds = 120;

    private final List<LoggingSaveRunnable> runnable_BTCe = new ArrayList();
    private final ConcurrentHashMap<String, ArrayList<TickerItemData>> list_BTCe;
    private final ReentrantLock mutex_btce = new ReentrantLock();

    public TickerCacheTask() {
        this.list_BTCe = new ConcurrentHashMap<>();
        StartScheduleTask();
    }

    public void StartScheduleTask() {
        for (String s : ChannelServer.getInstance().getCachingCurrencyPair()) {
            runnable_BTCe.add(TimerManager.register(new TickerCacheTask_BTCe(s), BTCe_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE));
        }
    }

    public void CancelAllTask() {
        for (LoggingSaveRunnable task : runnable_BTCe) {
            if (task != null) {
                task.getSchedule().cancel(true);
                task = null;
            }
        }
    }

    public ArrayList<TickerItemData> getListBTCe(final String ticker, String ExchangeSite) {
        return new ArrayList(list_BTCe.get(ExchangeSite + "-" + ticker));
    }

    public ArrayList<TickerItemData> getTickerList(final String ticker, final int hoursSelection, int depth, String ExchangeSite, long ServerTimeFrom) {
        final ArrayList<TickerItemData> list_BTCe2 = new ArrayList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;
        
        // Is the data set available?
        if (!list_BTCe.containsKey(dataSet)) {
            return list_BTCe2;
        }
        
        final long cTime = (System.currentTimeMillis() - (1000l * 60l * 60l * hoursSelection)) / 1000;
        
        int skipNumber = 0;
        float high = 0, low = Float.MAX_VALUE,
                buy = Float.MAX_VALUE, sell = 0;
        double Volume = 0, VolumeCur = 0;

        // No need to lock this thread, if we are creating a new ArrayList off existing.
        // Its a copy :)
        ArrayList<TickerItemData> currentList = new ArrayList(list_BTCe.get(dataSet));
        for (TickerItemData item : currentList) {
            long itemTime = item.getServerTime();

            if (cTime <= itemTime && itemTime >= ServerTimeFrom) {
                skipNumber++;

                if (skipNumber == depth) {
                    if (high != 0) { // default value = 0, so we'll fall back
                        item.setAvg(high + low / 2f);
                        item.setHigh(high);
                        item.setLow(low);
                        item.setBuy(buy);
                        item.setSell(sell);
                        item.setVol(Volume);
                        item.setVol_Cur(VolumeCur);
                    }

                    list_BTCe2.add(item);

                    // reset
                    high = 0;
                    low = Float.MAX_VALUE;
                    buy = Float.MAX_VALUE;
                    sell = 0;
                    Volume = 0;
                    VolumeCur = 0;

                    skipNumber = 0;
                } else {
                    if (item.getHigh() > high) {
                        high = item.getHigh();
                    }
                    if (item.getLow() < low) {
                        low = item.getLow();
                    }
                    if (item.getBuy() < buy) {
                        buy = item.getBuy();
                    }
                    if (item.getSell() > sell) {
                        sell = item.getSell();
                    }
                    if (item.getVol() > Volume) {
                        Volume = item.getVol();
                    }
                    if (item.getVol_Cur() > VolumeCur) {
                        VolumeCur = item.getVol();
                    }
                }
            }
        }
        return list_BTCe2;
    }
    
    public ArrayList<TickerItem_CandleBar> getTickerList_Candlestick(final String ticker, final int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom) {
        final ArrayList<TickerItem_CandleBar> list_BTCe2 = new ArrayList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;
        
        // Is the data set available?
        if (!list_BTCe.containsKey(dataSet)) {
            return list_BTCe2;
        }
        
        // Timestamp
        long cTime = (System.currentTimeMillis() / 1000l) - (60l * 60l * backtestHours);
        long cTime_Track = cTime - ((long) (60l * 60l * backtestHours * 0.5));
        long LastUsedTime = 0;

        /*  Close = (open + high + low + close) / 4
            High = maximum of high, open, or close (whichever is highest)
            Low = minimum of low, open, or close (whichever is lowest)
            Open = (open of previous bar + close of previous bar) / 2
        */
        
        double high = 0, low = Double.MAX_VALUE, open = -1;
        double Volume = 0, VolumeCur = 0;

        // No need to lock this thread, if we are creating a new ArrayList off existing.
        // Its a copy :)
        ArrayList<TickerItemData> currentList = new ArrayList(list_BTCe.get(dataSet));
        for (TickerItemData item : currentList) {
            long itemTime = item.getServerTime();

            if (cTime_Track <= itemTime && itemTime >= ServerTimeFrom) {
                if (LastUsedTime + (intervalMinutes * 60) < item.getServerTime()) {
                    if (item.getServerTime() > cTime) {
                        // If there's not enough data available.. 
                        if (high == 0) 
                            high = item.getSell();
                        if (low == Double.MAX_VALUE)
                            low = item.getBuy();
                        if (open == -1)
                            open = item.getBuy();
                        
                        // Add to list
                        list_BTCe2.add(
                                new TickerItem_CandleBar(item.getServerTime(), (float) item.getBuy(), (float) high, (float) low, (float) open, Volume, VolumeCur)
                        );
                    }
                    // reset
                    high = 0;
                    low = Double.MAX_VALUE;
                    open = -1;
                    Volume = 0;
                    VolumeCur = 0;
                    
                    if (LastUsedTime == 0)
                        LastUsedTime = item.getServerTime();
                    LastUsedTime = LastUsedTime + (intervalMinutes * 60);// item.getServerTime();
                } else {
                    high = Math.max(item.getSell(), high);
                    low = Math.min(item.getBuy(), low);
                    
                    if (open == -1)
                        open = item.getBuy();
                    
                    if (item.getVol() > Volume) {
                        Volume = item.getVol();
                    }
                    if (item.getVol_Cur() > VolumeCur) {
                        VolumeCur = item.getVol();
                    }
                }
            }
        }
        return list_BTCe2;
    }

    public List<ArrayList<ExponentialMovingAverageData>> getExponentialMovingAverage(
            final String ticker, final String ExchangeSite, int backtestHours, int intervalMinutes,
            final int HighEMA, final int LowEMA) {

        // check if exist
        if (!list_BTCe.containsKey(ExchangeSite + "-" + ticker)) {
            return null;
        }
        // add an extra 80% to the backtest hours to smooth out EMA at first
        long cTime = (System.currentTimeMillis() / 1000l) - (60l * 60l * backtestHours);
        long cTime_Track = cTime - ((long) (60l * 60l * backtestHours * 0.8d));
        long LastUsedTime = 0;

        // Gets the current array from cache
        final ArrayList<TickerItem> currentList = new ArrayList(list_BTCe.get(ExchangeSite + "-" + ticker)); // BTCChina

        // Create a new array to add things within our range we are looking for.
        final ArrayList<TickerItem> selectedList = new ArrayList();
        for (TickerItem item : currentList) {
            long itemTime = item.getServerTime();

            if (cTime_Track <= itemTime && LastUsedTime + (intervalMinutes * 60) < item.getServerTime()) {
                selectedList.add(item);

                if (LastUsedTime == 0)
                    LastUsedTime = item.getServerTime();
                LastUsedTime =  LastUsedTime + (intervalMinutes * 60); // item.getServerTime();
            }
        }

        final List<ArrayList<ExponentialMovingAverageData>> container = new ArrayList<>();
        container.add(ExponentialMovingAverage.CalculateEMA(selectedList, HighEMA, cTime));
        container.add(ExponentialMovingAverage.CalculateEMA(selectedList, LowEMA, cTime));

        /* System.out.println("High EMA");
         for (ExponentialMovingAverageData highEMA : container.get(0)) {
         System.out.println(highEMA.EMA);
         }
         System.out.println("Low EMA");
         for (ExponentialMovingAverageData highEMA : container.get(1)) {
         System.out.println(highEMA.EMA);
         }*/
        return container;
    }

    public class TickerCacheTask_BTCe implements Runnable {

        private final String CacheCurrencyPair;

        public TickerCacheTask_BTCe(String CacheCurrencyPair) {
            this.CacheCurrencyPair = CacheCurrencyPair;
        }

        @Override
        public void run() {
            System.out.println("Caching BTC-e price for currency : " + CacheCurrencyPair);

            long LastCachedTime = 0;
            if (list_BTCe.containsKey(CacheCurrencyPair)) {
                ArrayList<TickerItemData> currentList = list_BTCe.get(CacheCurrencyPair);

                if (!currentList.isEmpty())
                    LastCachedTime = currentList.get(currentList.size() - 1).getServerTime();
            }

            String[] Source_pair = CacheCurrencyPair.split("-");
            ArrayList<TickerItemData> list_BTCe2 = new ArrayList(); // create a new array first and replace later

            boolean result = AzureBitBot.btce_Select_Graph_Data(Source_pair[0], Source_pair[1], 20000, 24, LastCachedTime, list_BTCe2);
            if (!result) {
                return; // temporary network issue?
            }

            mutex_btce.lock();
            try {
                if (!list_BTCe.containsKey(CacheCurrencyPair)) {
                    list_BTCe.put(CacheCurrencyPair, list_BTCe2);
                } else {
                    ArrayList<TickerItemData> currentList = list_BTCe.get(CacheCurrencyPair);

                    currentList.addAll(list_BTCe2);
                }
            } finally {
                mutex_btce.unlock();
            }
            System.out.println("Caching price for "+CacheCurrencyPair+" --> " + list_BTCe2.size());

            /*ArrayList<TickerItem_BTCe> ret = ChannelServer.getInstance().getTickerTask().getListBTCe("xbt_usd", 24, 20, "kraken", 0);
             for (TickerItem_BTCe item : ret) {
             System.out.println("Buy: " + item.getBuy());
             System.out.println("Sell: " + item.getSell());
             System.out.println("High: " + item.getHigh());
             System.out.println("Low: " + item.getLow());
             System.out.println("Avg: " + item.getAvg());
             }*/
            //getExponentialMovingAverage("btc_cny", "btcchina", 900, 30, 43, 19);
        }
    }
}
