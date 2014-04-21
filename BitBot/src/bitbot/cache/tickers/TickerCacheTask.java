package bitbot.cache.tickers;

import bitbot.cache.tickers.history.BacklogCommitTask;
import bitbot.cache.tickers.history.HistoryDatabaseCommitEnum;
import bitbot.cache.tickers.history.TickerHistory;
import bitbot.cache.tickers.history.TickerHistoryData;
import bitbot.cache.tickers.history.TickerHistory_BTCChina;
import bitbot.cache.tickers.history.TickerHistory_BTCe;
import bitbot.cache.tickers.history.TickerHistory_BitFinex;
import bitbot.cache.tickers.history.TickerHistory_Bitstamp;
import bitbot.cache.tickers.history.TickerHistory_CampBX;
import bitbot.cache.tickers.history.TickerHistory_CexIo;
import bitbot.cache.tickers.history.TickerHistory_Coinbase;
import bitbot.cache.tickers.history.TickerHistory_Huobi;
import bitbot.cache.tickers.history.TickerHistory_ItBit;
import bitbot.cache.tickers.history.TickerHistory_Kraken;
import bitbot.cache.tickers.history.TickerHistory_MTGox;
import bitbot.cache.tickers.history.TickerHistory_Okcoin;
import bitbot.external.MicrosoftAzureExt;
import bitbot.graph.ExponentialMovingAverage;
import bitbot.graph.ExponentialMovingAverageData;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author z
 */
public class TickerCacheTask {

    // MSSQL
    private static final int MSSQL_CacheRefreshTime_Seconds = 60;

    private final List<LoggingSaveRunnable> runnable_mssql = new ArrayList();
    private final Map<String, List<TickerItemData>> list_mssql;
    private final ReentrantLock mutex_mssql = new ReentrantLock();

    // Acquiring of data directly from the trades 
    private final List<LoggingSaveRunnable> runnable_exchangeHistory = new ArrayList();

    public TickerCacheTask() {
        this.list_mssql = new LinkedHashMap<>();
        StartScheduleTask();
    }

    public void StartScheduleTask() {
        for (String ExchangeCurrencyPair : ChannelServer.getInstance().getCachingCurrencyPair()) {
            final String[] Source_pair = ExchangeCurrencyPair.split("-");
            final String ExchangeSite = Source_pair[0];
            final String CurrencyPair = Source_pair[1];

            // graph fetching from database
            if (ChannelServer.getInstance().isEnableSQLDataAcquisition()) {
                runnable_mssql.add(TimerManager.register(new TickerCacheTask_MSSql(ExchangeSite, CurrencyPair), MSSQL_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE));
            }

            // History
            if (ChannelServer.getInstance().isEnableTickerHistory()) {
                TickerHistory history = null;
                int UpdateTime = 10;

                if (ExchangeCurrencyPair.contains("huobi")) {
                    history = new TickerHistory_Huobi();
                    UpdateTime = 3;
                } else if (ExchangeCurrencyPair.contains("btce")) {
                    history = new TickerHistory_BTCe();
                } else if (ExchangeCurrencyPair.contains("btcchina")) {
                    history = new TickerHistory_BTCChina();
                } else if (ExchangeCurrencyPair.contains("bitstamp")) {
                    history = new TickerHistory_Bitstamp();
                } else if (ExchangeCurrencyPair.contains("kraken")) {
                    history = new TickerHistory_Kraken();
                } else if (ExchangeCurrencyPair.contains("okcoin")) {
                    history = new TickerHistory_Okcoin();
                } else if (ExchangeCurrencyPair.contains("itbit")) { // may need more work
                    history = new TickerHistory_ItBit();
                } else if (ExchangeCurrencyPair.contains("coinbase")) {
                    history = new TickerHistory_Coinbase();
                } else if (ExchangeCurrencyPair.contains("cexio")) {
                    history = new TickerHistory_CexIo();
                } else if (ExchangeCurrencyPair.contains("campbx")) {
                    history = new TickerHistory_CampBX();
                } else if (ExchangeCurrencyPair.contains("bitfinex")) {
                    history = new TickerHistory_BitFinex();

                } else if (ExchangeCurrencyPair.contains("mtgox")) {
                    history = new TickerHistory_MTGox();
                }
                //bitfinex-btc_usd---kraken-xbt_usd---kraken-xbt_eur---cexio-ghs_btc

                if (history != null) {
                    runnable_exchangeHistory.add(TimerManager.register(
                            new TickerCacheTask_ExchangeHistory(ExchangeSite, CurrencyPair, ExchangeCurrencyPair, history),
                            UpdateTime * 1000, Integer.MAX_VALUE));
                }
            }
        }
    }

    public void CancelAllTask() {
        for (LoggingSaveRunnable task : runnable_mssql) {
            if (task != null) {
                task.getSchedule().cancel(true);
                task = null;
            }
        }
        for (LoggingSaveRunnable task : runnable_exchangeHistory) {
            if (task != null) {
                task.getSchedule().cancel(true);
                task = null;
            }
        }
    }

    public List<TickerItemData> getListBTCe(final String ticker, String ExchangeSite) {
        return new LinkedList(list_mssql.get(ExchangeSite + "-" + ticker));
    }

    public List<TickerItemData> getTickerList(final String ticker, final int hoursSelection, int depth, String ExchangeSite, long ServerTimeFrom) {
        final List<TickerItemData> list_BTCe2 = new LinkedList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return list_BTCe2;
        }

        final long cTime = (System.currentTimeMillis() - (1000l * 60l * 60l * hoursSelection)) / 1000;

        int skipNumber = 0;
        float high = 0, low = Float.MAX_VALUE,
                buy = Float.MAX_VALUE;
        double Volume = 0, VolumeCur = 0;

        // No need to lock this thread, if we are creating a new ArrayList off existing.
        // Its a copy :)
        List<TickerItemData> currentList = new LinkedList(list_mssql.get(dataSet));

        Iterator<TickerItemData> itr = currentList.iterator();
        while (itr.hasNext()) { // Loop through things in proper sequence
            TickerItemData item = itr.next();

            long itemTime = item.getServerTime();

            if (cTime <= itemTime && itemTime >= ServerTimeFrom) {
                skipNumber++;

                if (skipNumber == depth) {
                    if (high != 0) { // default value = 0, so we'll fall back
                        item.setHigh(high);
                        item.setLow(low);
                        item.setOpen(buy);
                        item.setVol(Volume);
                        item.setVol_Cur(VolumeCur);
                    }

                    list_BTCe2.add(item);

                    // reset
                    high = 0;
                    low = Float.MAX_VALUE;
                    buy = Float.MAX_VALUE;
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
                    if (item.getOpen() < buy) {
                        buy = item.getOpen();
                    }
                    if (item.getVol() > Volume) {
                        Volume = item.getVol();
                    }
                    if (item.getVol_Cur() > VolumeCur) {
                        VolumeCur = item.getVol_Cur();
                    }
                }
            }
        }
        return list_BTCe2;
    }

    public List<TickerItem_CandleBar> getTickerList_Candlestick(final String ticker, final int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom) {
        final List<TickerItem_CandleBar> list_BTCe2 = new LinkedList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return list_BTCe2;
        }
        // Timestamp
        long cTime = (System.currentTimeMillis() / 1000l) - (60l * 60l * backtestHours);
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
        List<TickerItemData> currentList = new LinkedList(list_mssql.get(dataSet));

        Iterator<TickerItemData> itr = currentList.iterator();
        while (itr.hasNext()) { // Loop through things in proper sequence
            TickerItemData item = itr.next();

            if (item.getServerTime() >= ServerTimeFrom) {
                // Check if last added tick is above the threshold 'intervalMinutes'
                if (LastUsedTime + (intervalMinutes * 60) < item.getServerTime()) {
                    while (LastUsedTime + (intervalMinutes * 60) < item.getServerTime()) {
                        if (item.getServerTime() > cTime) {
                            // If there's not enough data available.. 
                            if (high == 0) {
                                high = item.getHigh();
                            }
                            if (low == Double.MAX_VALUE) {
                                low = item.getLow();
                            }
                            if (open == -1) {
                                open = item.getOpen();
                            }
                            if (LastUsedTime == 0) {
                                LastUsedTime = item.getServerTime();
                            }
                            if (item.getVol() == 0) {
                                Volume = item.getVol();
                            }
                            if (item.getVol_Cur() == 0) {
                                VolumeCur = item.getVol_Cur();
                            }
                            // Add to list
                            list_BTCe2.add(
                                    new TickerItem_CandleBar(
                                            LastUsedTime + (intervalMinutes * 60),
                                            (float) item.getClose() == 0 ? item.getOpen() : item.getClose(),
                                            (float) high,
                                            (float) low,
                                            (float) open,
                                            Volume,
                                            VolumeCur)
                            );
                        }
                        // reset
                        high = 0;
                        low = Double.MAX_VALUE;
                        open = -1;
                        Volume = 0;
                        VolumeCur = 0;

                        if (LastUsedTime == 0) {
                            LastUsedTime = item.getServerTime();
                        }
                        LastUsedTime = LastUsedTime + (intervalMinutes * 60);// item.getServerTime();
                    }
                } else {
                    high = Math.max(item.getHigh(), high);
                    low = Math.min(item.getLow(), low);

                    if (open == -1) {
                        open = item.getOpen();
                    }
                    if (item.getVol() > Volume) {
                        Volume = item.getVol();
                    }
                    if (item.getVol_Cur() > VolumeCur) {
                        VolumeCur = item.getVol_Cur();
                    }
                }
            }
        }
        return list_BTCe2;
    }

    public List<List<ExponentialMovingAverageData>> getExponentialMovingAverage(
            final String ticker, final String ExchangeSite, int backtestHours, int intervalMinutes,
            final int HighEMA, final int LowEMA) {
        final String dataSet = ExchangeSite + "-" + ticker;

        // check if exist
        if (!list_mssql.containsKey(dataSet)) {
            return null;
        }
        // add an extra 80% to the backtest hours to smooth out EMA at first
        long cTime = (System.currentTimeMillis() / 1000l) - (60l * 60l * backtestHours);
        long cTime_Track = cTime - ((long) (60l * 60l * backtestHours * 0.8d));
        long LastUsedTime = 0;

        // Gets the current array from cache
        final List<TickerItem> currentList = new LinkedList(list_mssql.get(dataSet));

        // Create a new array to add things within our range we are looking for.
        final List<TickerItem> selectedList = new LinkedList();

        Iterator<TickerItem> itr = currentList.iterator();
        while (itr.hasNext()) { // Loop through things in proper sequence
            TickerItem item = itr.next();

            long itemTime = item.getServerTime();

            if (cTime_Track <= itemTime && LastUsedTime + (intervalMinutes * 60) < item.getServerTime()) {
                selectedList.add(item);

                if (LastUsedTime == 0) {
                    LastUsedTime = item.getServerTime();
                }
                LastUsedTime = LastUsedTime + (intervalMinutes * 60); // item.getServerTime();
            }
        }

        final List<List<ExponentialMovingAverageData>> container = new LinkedList<>();
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

    public class TickerCacheTask_ExchangeHistory implements Runnable {

        private final String CurrencyPair;
        private final String ExchangeSite;
        private final String ExchangeCurrencyPair;

        private final TickerHistory HistoryConnector;

        private boolean IsLoading;
        private long LastCommitTime;
        private TickerHistoryData HistoryData;

        public TickerCacheTask_ExchangeHistory(String ExchangeSite, String CurrencyPair, String ExchangeCurrencyPair, TickerHistory HistoryConnector) {
            this.CurrencyPair = CurrencyPair;
            this.ExchangeSite = ExchangeSite;
            this.ExchangeCurrencyPair = ExchangeCurrencyPair;
            this.IsLoading = false;
            this.LastCommitTime = 0;
            this.HistoryConnector = HistoryConnector;
        }

        @Override
        public void run() {
            if (IsLoading) // prevent double-caching of data at that instance
            {
                return; // don't want to lock this thread anyway, if one is delayed so be it.
            }
            IsLoading = true;

            System.out.println(String.format("[TH] Updating price: %s", ExchangeCurrencyPair));

            TickerHistoryData data = HistoryConnector.connectAndParseHistoryResult(
                    ExchangeCurrencyPair,
                    CurrencyPair,
                    HistoryData != null ? HistoryData.getLastPurchaseTime() : 0); // Read from buy/sell history

            if (data != null) { // Network unavailable?
                if (HistoryData != null) {
                    HistoryData.merge(data); // Merge high + lows, volume and set last date where it is read
                } else {
                    HistoryData = data;
                }
                HistoryDatabaseCommitEnum commitResult = HistoryData.commitDatabase(LastCommitTime, ExchangeSite, CurrencyPair);
                switch (commitResult) {
                    case Ok: {
                        // Output
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(HistoryData.getLastPurchaseTime());
                        cal.set(Calendar.SECOND, 0);

                        System.out.println(String.format("[TH] %s Commited data hh:mm = (%s), High: %f, Low: %f, Volume: %f, VolumeCur: %f",
                                ExchangeCurrencyPair, cal.getTime().toString(), HistoryData.getHigh(), HistoryData.getLow(), HistoryData.getVolume(), HistoryData.getVolume_Cur()));

                        LastCommitTime = HistoryData.getLastPurchaseTime();

                        // Set new
                        HistoryData = new TickerHistoryData(HistoryData.getLastPurchaseTime(), HistoryData.isCoinbase_CampBX_CexIO());
                        HistoryData.setOpen(HistoryData.getLastPrice());
                        break;
                    }
                    case DatabaseError: { // Save to local cache for now until database is available once again.
                        BacklogCommitTask.RegisterForLogging(HistoryData); // 15 minute task, backlog commit

                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(HistoryData.getLastPurchaseTime());
                        cal.set(Calendar.SECOND, 0);

                        // Output
                        System.out.println("[TH] Failed commit data hh:mm = (" + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + "), High: " + HistoryData.getHigh() + ", Low: " + HistoryData.getLow() + ", Volume: " + HistoryData.getVolume());

                        LastCommitTime = HistoryData.getLastPurchaseTime();

                        // Set new
                        HistoryData = new TickerHistoryData(HistoryData.getLastPurchaseTime(), HistoryData.isCoinbase_CampBX_CexIO());
                        HistoryData.setOpen(HistoryData.getLastPrice());
                        break;
                    }
                    case Time_Not_Ready: { // nth to do
                        break;
                    }
                }
            }
            IsLoading = false;
        }
    }

    public class TickerCacheTask_MSSql implements Runnable {

        private final String CurrencyPair_;
        private final String ExchangeSite;
        private long LastCachedTime = 0;

        public TickerCacheTask_MSSql(String ExchangeSite, String CacheCurrencyPair) {
            this.CurrencyPair_ = CacheCurrencyPair;
            this.ExchangeSite = ExchangeSite;
        }

        @Override
        public void run() {
            System.out.println("Caching currency pair from SQLserv: " + ExchangeSite + ":" + CurrencyPair_);

            List<TickerItemData> list_BTCe2 = new LinkedList(); // create a new array first and replace later
            boolean result = MicrosoftAzureExt.btce_Select_Graph_Data(ExchangeSite, CurrencyPair_, 999999, 24, LastCachedTime, list_BTCe2);
            if (!result) {
                return; // temporary network issue?
            }
            final String ExchangeCurrencyPair = String.format("%s-%s", ExchangeSite, CurrencyPair_);

            mutex_mssql.lock();
            try {
                if (!list_mssql.containsKey(ExchangeCurrencyPair)) {
                    list_mssql.put(ExchangeCurrencyPair, list_BTCe2);

                    for (TickerItemData data : list_BTCe2) {
                        if (data.getServerTime() > LastCachedTime) {
                            LastCachedTime = data.getServerTime();
                        }
                    }
                } else {
                    List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);
                    for (TickerItemData data : list_BTCe2) {
                        if (data.getServerTime() > LastCachedTime) {
                            currentList.add(data);

                            LastCachedTime = data.getServerTime();
                        }
                    }
                }
            } finally {
                mutex_mssql.unlock();
            }
            System.out.println("Caching price for " + ExchangeCurrencyPair + " --> " + list_BTCe2.size());
        }
    }
}
