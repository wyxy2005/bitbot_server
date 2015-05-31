package bitbot.cache.tickers;

import bitbot.cache.tickers.HTTP.TickerHistory_Dgex;
import bitbot.cache.tickers.HTTP.TickerHistory_CexIo;
import bitbot.cache.tickers.HTTP.TickerHistory_ItBit;
import bitbot.cache.tickers.HTTP.TickerHistory_Okcoin;
import bitbot.cache.tickers.HTTP.TickerHistory_BTCe;
import bitbot.cache.tickers.HTTP.TickerHistory_OkcoinInternational;
import bitbot.cache.tickers.HTTP.TickerHistory_Kraken;
import bitbot.cache.tickers.HTTP.TickerHistory_CampBX;
import bitbot.cache.tickers.HTTP.TickerHistory_BTCChina;
import bitbot.cache.tickers.HTTP.TickerHistory_Coinbase;
import bitbot.cache.tickers.HTTP.TickerHistory_BitFinex;
import bitbot.cache.tickers.HTTP.TickerHistory_Huobi;
import bitbot.cache.tickers.HTTP.TickerHistory_FybSGSE;
import bitbot.cache.tickers.HTTP.TickerHistory_Bitstamp;
import bitbot.cache.tickers.HTTP.TickerHistory_Cryptsy;
import bitbot.cache.tickers.HTTP.TickerHistory_796;
import bitbot.cache.tickers.HTTP.TickerHistory_CoinbaseExchange;
import bitbot.external.MicrosoftAzureDatabaseExt;
import bitbot.graph.ExponentialMovingAverage;
import bitbot.graph.ExponentialMovingAverageData;
import bitbot.handler.channel.ChannelServer;
import bitbot.Constants;
import bitbot.cache.tickers.HTTP.TickerHistory_BitVC;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import bitbot.util.encryption.input.ByteArrayByteStream;
import bitbot.util.encryption.input.GenericSeekableLittleEndianAccessor;
import bitbot.util.encryption.input.SeekableLittleEndianAccessor;
import bitbot.util.encryption.output.PacketLittleEndianWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.time.DateUtils;

/**
 *
 * @author z
 */
public class TickerCacheTask {

    // MSSQL
    private static final int MSSQL_CacheRefreshTime_Seconds = 60;

    // Acquiring of old data from mssql database
    private final List<Integer> canAcceptNewInfoFromOtherPeers = new LinkedList(); // string = exchange+currency pair
    private final List<LoggingSaveRunnable> runnable_mssql = new ArrayList();
    private final Map<String, List<TickerItemData>> list_mssql;
    private final Map<String, List<TickerItemData>> list_mssql_summary; // Create another dummy map for easy reference [summary]

    // Acquiring of data directly from the trades
    private final List<LoggingSaveRunnable> runnable_exchangeHistory = new ArrayList();

    public TickerCacheTask() {
        this.list_mssql = new LinkedHashMap<>();
        this.list_mssql_summary = new LinkedHashMap<>();
        
        StartScheduleTask();
    }

    public void StartScheduleTask() {
        for (String ExchangeCurrencyPair : ChannelServer.getInstance().getCachingCurrencyPair()) {
            final String[] Source_pair = ExchangeCurrencyPair.split("-");
            final String ExchangeSite = Source_pair[0];
            final String CurrencyPair = Source_pair[1];

            // graph fetching from database
            if (ChannelServer.getInstance().isEnableSQLDataAcquisition()) {
                TickerCacheTask_MSSql tickercache = new TickerCacheTask_MSSql(ExchangeCurrencyPair, ExchangeSite, CurrencyPair);
                LoggingSaveRunnable runnable = TimerManager.register(tickercache, MSSQL_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE);

                // set this reference so we are able to cancel this task later when we dont need the database anymore
                tickercache.setLoggingSaveRunnable(runnable);

                runnable_mssql.add(runnable);

                List<TickerItemData> arrays = new ArrayList(); // same reference
                list_mssql.put(ExchangeCurrencyPair, arrays);
                list_mssql.put(ExchangeCurrencyPair.toLowerCase(), arrays); // to fix for futures
                list_mssql.put(ExchangeCurrencyPair.replace(" ", "").toLowerCase(), arrays); // and one without space, for hyperlinks
                
                list_mssql_summary.put(ExchangeCurrencyPair, arrays);
            }

            // History
            if (ChannelServer.getInstance().isEnableTickerHistory()) {
                TickerHistoryInterface history = null;
                int UpdateTime_Millis = 10000;
                
                final boolean trackLargeTrades = ChannelServer.getInstance().getCurrencyPairsForLargeTrades().contains(ExchangeCurrencyPair);

                if (ExchangeCurrencyPair.contains("huobi")) {
                    history = new TickerHistory_Huobi(trackLargeTrades);
                    UpdateTime_Millis = 500;

                } else if (ExchangeCurrencyPair.contains("bitvc")) {
                    history = new TickerHistory_BitVC(trackLargeTrades);
                    UpdateTime_Millis = 10000;

                } else if (ExchangeCurrencyPair.contains("btce")) {
                    history = new TickerHistory_BTCe(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("btcchina")) {
                    history = new TickerHistory_BTCChina(trackLargeTrades);
                    UpdateTime_Millis = 1000;

                } else if (ExchangeCurrencyPair.contains("bitstamp")) {
                    history = new TickerHistory_Bitstamp(trackLargeTrades);
                    UpdateTime_Millis = 2000;

                } else if (ExchangeCurrencyPair.contains("kraken")) {
                    history = new TickerHistory_Kraken(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("okcoin")) {
                    if (ExchangeCurrencyPair.contains("okcoininternational")) {
                        history = new TickerHistory_OkcoinInternational(trackLargeTrades);
                        UpdateTime_Millis = 1000;
                    } else {
                        history = new TickerHistory_Okcoin(trackLargeTrades);
                        UpdateTime_Millis = 500;
                    }

                } else if (ExchangeCurrencyPair.contains("fybsg") || ExchangeCurrencyPair.contains("fybse")) {
                    history = new TickerHistory_FybSGSE(trackLargeTrades);
                    UpdateTime_Millis = 15000; // volume is still too low to make an impact

                } else if (ExchangeCurrencyPair.contains("itbit")) { // may need more work
                    history = new TickerHistory_ItBit(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("coinbase")) {
                    if (ExchangeCurrencyPair.contains("coinbaseexchange")) {
                        history = new TickerHistory_CoinbaseExchange(trackLargeTrades);
                        UpdateTime_Millis = 5000;
                    } else {
                        history = new TickerHistory_Coinbase(trackLargeTrades);
                        UpdateTime_Millis = 15000; // Coinbase is just a broker....
                    }

                } else if (ExchangeCurrencyPair.contains("cexio")) {
                    history = new TickerHistory_CexIo(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("campbx")) {
                    history = new TickerHistory_CampBX(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("bitfinex")) {
                    history = new TickerHistory_BitFinex(trackLargeTrades);
                    UpdateTime_Millis = 1000;

                } else if (ExchangeCurrencyPair.contains("dgex")) {
                    history = new TickerHistory_Dgex(trackLargeTrades);
                    UpdateTime_Millis = 15000; // volume is still too low to make an impact

                } else if (ExchangeCurrencyPair.contains("cryptsy")) {
                    history = new TickerHistory_Cryptsy(trackLargeTrades);

                } else if (ExchangeCurrencyPair.contains("796")) {
                    history = new TickerHistory_796(trackLargeTrades);
                    UpdateTime_Millis = 1000;

                } else if (ExchangeCurrencyPair.contains("mtgox")) { // goxxed
                    //history = new TickerHistory_MTGox(trackLargeTrades); // died
                }

                if (history != null) {
                    runnable_exchangeHistory.add(TimerManager.register(
                            new TickerCacheTask_ExchangeHistory(ExchangeSite, CurrencyPair, ExchangeCurrencyPair, history),
                            UpdateTime_Millis, Integer.MAX_VALUE));
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
        for (Iterator<LoggingSaveRunnable> it = runnable_exchangeHistory.iterator(); it.hasNext();) {
            LoggingSaveRunnable task = it.next();
            if (task != null) {
                task.getSchedule().cancel(true);
                task = null;
            }
        }
    }

    public List<TickerItemData> getListBTCe(final String ticker, String ExchangeSite) {
        return new ArrayList(list_mssql.get(ExchangeSite + "-" + ticker));
    }

    public TickerItemData getTickerSummary(final String ticker, String ExchangeSite) {
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return null;
        }
        final List<TickerItemData> currentList = list_mssql.get(dataSet);
        if (currentList.isEmpty()) {
            return null;
        }
        return currentList.get(currentList.size() - 1);
    }

    public List<TickerItem_CandleBar> getTickerList_Candlestick(
            final String ticker, 
            final int backtestHours, 
            int intervalMinutes, 
            String ExchangeSite, 
            long ServerTimeFrom, 
            long ServerTimeEnd,
            boolean IncludeUnmaturedData) {
        
        final List<TickerItem_CandleBar> list_chart = new ArrayList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return list_chart;
        }

        // Timestamp
        final long cTime_Millis = System.currentTimeMillis();
        intervalMinutes *= 60; // convert to seconds

        final boolean includeVolumeData = !ExchangeSite.equalsIgnoreCase("coinbase");

        final long cTime = cTime_Millis / 1000;
        
        // truncate the ending time to max current time
        // This could also prevent DOS
        ServerTimeEnd = Math.min(cTime_Millis / 1000, ServerTimeEnd);

        // Determine where to start the candlestick
        final long LastUsedTime_;
        long LastUsedTime = 0;
        if (ServerTimeFrom != 0) { // tradingview
            LastUsedTime_ = ServerTimeFrom;
        } else {
            LastUsedTime_ = cTime - (60l * 60l * backtestHours);
        }
        LastUsedTime = LastUsedTime_;

        float high = 0, low = Float.MAX_VALUE, open = -1, lastPriceSet = 0;
        double Volume = 0, VolumeCur = 0;
        float buysell_ratio_Total = 0, buysellratio_sets = 1;

        // No need to lock this thread, if we are creating a new ArrayList off existing since its a copy :)
        // Its a copy :)
        final Stream<TickerItemData> items_stream = list_mssql.get(dataSet).stream().
                filter((data) -> (data.getServerTime() >= LastUsedTime_)).
                sorted(TickerItemComparator);

        boolean isPostProcessCompleted = false;
        // Now we create the candle data chain
        final Iterator<TickerItemData> items = items_stream.iterator();
        while (items.hasNext()) {
            TickerItemData item = items.next();

            if (!isPostProcessCompleted) {
                isPostProcessCompleted = true;

               // if (backtestHours != 0) { // not tradingview
                    // Post process, for the variables and stuff
                    // round the last used time to best possible time for the chart period
                    final Calendar dtCal = Calendar.getInstance();
                    dtCal.setTimeInMillis(Math.min(LastUsedTime, item.getServerTime()));

                    int truncateField;
                    if (intervalMinutes < 60) { // below 1 hour
                        truncateField = Calendar.HOUR;
                    } else if (intervalMinutes < 60 * 60 * 24) { // below 1 day
                        truncateField = Calendar.DATE;
                    } else if (intervalMinutes < 60 * 60 * 24 * 30) { // below 30 days
                        truncateField = Calendar.MONTH;
                    } else if (intervalMinutes < 60 * 60 * 24 * 30 * 12 * 100) { // below 100 years
                        truncateField = Calendar.YEAR;
                    } else { // wtf
                        truncateField = Calendar.ERA;
                    }
                    LastUsedTime = DateUtils.truncate(dtCal, truncateField).getTimeInMillis();
              //  }
                while (LastUsedTime < item.getServerTime()) {
                    LastUsedTime += intervalMinutes;
                }
            }

            // Check if last added tick is above the threshold 'intervalMinutes'
            if (LastUsedTime + intervalMinutes < item.getServerTime()) {
                boolean isInstanceValueAdded = false;

                while (LastUsedTime + intervalMinutes < item.getServerTime()) {
                    // If there's not enough data available..
                    if (!isInstanceValueAdded) {
                        if (high == 0) {
                            high = item.getHigh();
                        }
                        if (low == Float.MAX_VALUE) {
                            low = item.getLow();
                        }
                        if (open == -1) {
                            open = item.getOpen();
                        }
                        if (LastUsedTime == 0) {
                            LastUsedTime = item.getServerTime();
                        }
                        if (includeVolumeData) {
                            if (Volume == 0) {
                                Volume = item.getVol();
                            }
                            if (VolumeCur == 0) {
                                VolumeCur = item.getVol_Cur();
                            }
                        }
                    } else {
                        // price = close because there are no trading during this time, so its based on the last closing price
                        if (high == 0) {
                            high = item.getClose();
                        }
                        if (low == Float.MAX_VALUE) {
                            low = item.getClose();
                        }
                        if (open == -1) {
                            open = item.getClose();
                        }
                        if (LastUsedTime == 0) {
                            LastUsedTime = item.getServerTime();
                        }
                    }
                    lastPriceSet = item.getClose();

                    // Add to list
                    final long DataTime = LastUsedTime + intervalMinutes;
                    list_chart.add(
                            new TickerItem_CandleBar(
                                    DataTime,
                                    (float) item.getClose() == 0 ? item.getOpen() : item.getClose(),
                                    (float) high,
                                    (float) low,
                                    (float) open,
                                    Volume,
                                    VolumeCur,
                                    (buysell_ratio_Total == 0 ? 1 : buysell_ratio_Total) / buysellratio_sets, false)); // TODO: Fix buy/sell ratio here, 0 / something > 1 returns null on JSON result

                    isInstanceValueAdded = true;

                    // all we need here.
                    if (DataTime > ServerTimeEnd) {
                        return list_chart;
                    }

                    // reset
                    high = 0;
                    low = Float.MAX_VALUE;
                    Volume = 0;
                    VolumeCur = 0;
                    open = item.getClose() != 0 ? item.getClose() : item.getOpen(); // Next open = current close.
                    buysellratio_sets = 1;
                    buysell_ratio_Total = 0;

                    if (LastUsedTime == 0) {
                        LastUsedTime = item.getServerTime();
                    }
                    LastUsedTime += intervalMinutes;
                }
            } else {
                high = Math.max(item.getHigh(), high);
                low = Math.min(item.getLow(), low);

                if (open == -1) {
                    open = item.getOpen();
                }
                if (includeVolumeData) {
                    Volume += item.getVol();
                    VolumeCur += item.getVol_Cur();
                }

                buysell_ratio_Total += item.getBuySell_Ratio();
                buysellratio_sets++;

                lastPriceSet = item.getClose();
            }
        }

        if (IncludeUnmaturedData) {
            // For unmatured chart
            while (LastUsedTime < ServerTimeEnd) {
                LastUsedTime += intervalMinutes;

                if (LastUsedTime > ServerTimeEnd) { // check again
                    break; // must not ever sent a candle over the current time!!
                }

                list_chart.add(
                        new TickerItem_CandleBar(
                                LastUsedTime,
                                (float) lastPriceSet, // last
                                (float) lastPriceSet, // high
                                (float) lastPriceSet, // low
                                (float) lastPriceSet, // open
                                0,// volume
                                0, // volume cur
                                1, // buy sell ratio
                                true)
                );
            }
        }
        return list_chart;
    }

    public List<ReturnVolumeProfileData> getVolumeProfile(final String ticker, final List<Integer> hoursFromNow, String ExchangeSite) {
        List<ReturnVolumeProfileData> ret = new ArrayList();

        for (Integer i : hoursFromNow) {
            ReturnVolumeProfileData profile = getVolumeProfileInternal(ticker, i, ExchangeSite);

            ret.add(profile);
        }
        return ret;
    }

    private ReturnVolumeProfileData getVolumeProfileInternal(final String ticker, final int hoursFromNow, String ExchangeSite) {
        final ReturnVolumeProfileData profile = new ReturnVolumeProfileData();
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return profile;
        }
        // Timestamp
        final long cTime_Millis = System.currentTimeMillis();
        final long cTime = cTime_Millis / 1000;
        final long startTime = cTime - (hoursFromNow * 60l * 60l);

        double totalBuyVolume_Cur = 0, totalSellVolume_Cur = 0;
        double totalBuyVolume = 0, totalSellVolume = 0;

        // No need to lock this thread, if we are creating a new ArrayList off existing.
        // Its a copy :)
        final List<TickerItemData> currentList = list_mssql.get(dataSet);

        //System.out.println("Start time: " + startTime + " , Cur time: " + cTime + " , " + currentList.get(currentList.size() - 1).getServerTime());
        final Iterator<TickerItemData> items = currentList.stream().
                filter((data) -> (data.getServerTime() > startTime)).
                sorted(TickerItemComparator).
                iterator();

        while (items.hasNext()) {
            TickerItemData item = items.next();
//System.out.println("["+item.getServerTime()+"] " + item.getBuySell_Ratio());
            if (item.getBuySell_Ratio() != 0f && item.getBuySell_Ratio() != 1.0f) {
                float buyAndSellRatio = item.getBuySell_Ratio() + 1.0f;

                // volume cur
                double buyVolumeCur = (item.getVol_Cur() / buyAndSellRatio) * item.getBuySell_Ratio();
                double sellVolumeCur = item.getVol_Cur() - buyVolumeCur;

                totalBuyVolume_Cur += buyVolumeCur;
                totalSellVolume_Cur += sellVolumeCur;

                // volume
                double buyVolume = (item.getVol() / buyAndSellRatio) * item.getBuySell_Ratio();
                double sellVolume = item.getVol() - buyVolume;

                totalBuyVolume += buyVolume;
                totalSellVolume += sellVolume;
            }
        }
        profile.TotalBuyVolume_Cur = totalBuyVolume_Cur;
        profile.TotalSellVolume_Cur = totalSellVolume_Cur;

        profile.TotalBuyVolume = totalBuyVolume;
        profile.TotalSellVolume = totalSellVolume;

        return profile;
    }

    private ReturnVolumeProfileData getVolumeProfileByPrice_Internal(final String ticker, final int hoursFromNow, String ExchangeSite) {
        final ReturnVolumeProfileData profile = new ReturnVolumeProfileData();
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return profile;
        }
        // Timestamp
        final long cTime_Millis = System.currentTimeMillis();
        final long cTime = cTime_Millis / 1000;
        final long startTime = cTime - (hoursFromNow * 60l * 60l);

        double totalBuyVolume_Cur = 0, totalSellVolume_Cur = 0;
        double totalBuyVolume = 0, totalSellVolume = 0;

        // No need to lock this thread, if we are creating a new ArrayList off existing.
        // Its a copy :)
        final List<TickerItemData> currentList = list_mssql.get(dataSet);

        //System.out.println("Start time: " + startTime + " , Cur time: " + cTime + " , " + currentList.get(currentList.size() - 1).getServerTime());
        final Iterator<TickerItemData> items = currentList.stream().
                filter((data) -> (data.getServerTime() > startTime)).
                sorted(TickerItemComparator).
                iterator();

        while (items.hasNext()) {
            TickerItemData item = items.next();
//System.out.println("["+item.getServerTime()+"] " + item.getBuySell_Ratio());
            if (item.getBuySell_Ratio() != 0f && item.getBuySell_Ratio() != 1.0f) {
                float buyAndSellRatio = item.getBuySell_Ratio() + 1.0f;

                // volume cur
                double buyVolumeCur = (item.getVol_Cur() / buyAndSellRatio) * item.getBuySell_Ratio();
                double sellVolumeCur = item.getVol_Cur() - buyVolumeCur;

                totalBuyVolume_Cur += buyVolumeCur;
                totalSellVolume_Cur += sellVolumeCur;

                // volume
                double buyVolume = (item.getVol() / buyAndSellRatio) * item.getBuySell_Ratio();
                double sellVolume = item.getVol() - buyVolume;

                totalBuyVolume += buyVolume;
                totalSellVolume += sellVolume;
            }
        }
        profile.TotalBuyVolume_Cur = totalBuyVolume_Cur;
        profile.TotalSellVolume_Cur = totalSellVolume_Cur;

        profile.TotalBuyVolume = totalBuyVolume;
        profile.TotalSellVolume = totalSellVolume;

        return profile;
    }

    /**
     * Returns the summary data of all exchange/pair ticker
     *
     *
     * @return a map containing the TickerItemData, with the key being
     * exchange_currencyPair
     * @see TickerItemData
     */
    public Map<String, TickerItemData> getExchangePriceSummaryData() {
        final Map<String, TickerItemData> mapPriceSummary = new HashMap();

        list_mssql_summary.entrySet().stream().forEach((mapItem) -> {
            mapPriceSummary.put(mapItem.getKey(), 
                    mapItem.getValue().size() > 0 ? mapItem.getValue().get(mapItem.getValue().size() - 1) : null);
        });

        return mapPriceSummary;
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
        final List<TickerItem> currentList = new ArrayList(list_mssql.get(dataSet));

        // Create a new array to add things within our range we are looking for.
        final List<TickerItem> selectedList = new ArrayList();

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

        final List<List<ExponentialMovingAverageData>> container = new ArrayList<>();
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
    

    private static final Comparator<Object> TickerItemComparator = (Object obj1, Object obj2) -> {
        TickerItemData data1 = (TickerItemData) obj1;
        TickerItemData data2 = (TickerItemData) obj2;

        if (data1.getServerTime() > data2.getServerTime()) {
            return 1;
        } else if (data1.getServerTime() == data2.getServerTime()) {
            return 0;
        }
        return -1;
    };

    public class TickerCacheTask_ExchangeHistory implements Runnable {

        private final String CurrencyPair;
        private final String ExchangeSite;
        private final String ExchangeCurrencyPair;

        private final TickerHistoryInterface HistoryConnector;

        private boolean IsLoading;
        private long LastCommitTime;
        private TickerHistoryData HistoryData;
        private long lastCacheTime = 0;

        private long lastBroadcastedTime = 0;

        private boolean readyToBroadcastPriceChanges() {
            final long cTime = System.currentTimeMillis();
            if (cTime - lastBroadcastedTime > Constants.PriceBetweenServerBroadcastDelay) {
                lastBroadcastedTime = cTime;
                return true;
            }
            return false;
        }

        public TickerCacheTask_ExchangeHistory(String ExchangeSite, String CurrencyPair, String ExchangeCurrencyPair, TickerHistoryInterface HistoryConnector) {
            this.ExchangeSite = ExchangeSite;
            this.CurrencyPair = CurrencyPair;
            this.ExchangeCurrencyPair = ExchangeCurrencyPair;
            this.IsLoading = false;
            this.LastCommitTime = 0;
            this.HistoryConnector = HistoryConnector;
        }

        @Override
        public void run() {
            long cTime = System.currentTimeMillis();
            if (IsLoading) // prevent double-caching of data at that instance
            {
                if (cTime - lastCacheTime < 20000) { // below 20 seconds ago... 
                    return; // don't want to lock this thread anyway, if one is delayed so be it.
                }
            }
            lastCacheTime = cTime;
            IsLoading = true;

            System.out.println(String.format("[TH] Updating price: %s", ExchangeCurrencyPair));

            try {
                TickerHistoryData data = HistoryConnector.connectAndParseHistoryResult(
                        ExchangeCurrencyPair,
                        ExchangeSite,
                        CurrencyPair,
                        HistoryData != null ? HistoryData.getLastPurchaseTime() : 0, // Read from buy/sell history
                        HistoryData != null ? HistoryData.getLastTradeId() : 0);

                if (data != null) { // Network unavailable?
                    if (HistoryData != null) {
                        HistoryData.merge(data); // Merge high + lows, volume and set last date where it is read
                    } else {
                        HistoryData = data;
                    }
                    HistoryDatabaseCommitEnum commitResult = HistoryData.tryCommitDatabase(LastCommitTime, ExchangeSite, CurrencyPair, ExchangeCurrencyPair);

                    // Broadcast this piece of data to world server 
                    if (HistoryData.getLastPrice() != 0 && readyToBroadcastPriceChanges()) {
                        ChannelServer.getInstance().broadcastPriceChanges(
                                ExchangeCurrencyPair,
                                HistoryData.getLastPurchaseTime() / 1000l,
                                HistoryData.getLastPrice(), // using last price as close since this isnt known yet
                                HistoryData.getHigh(), HistoryData.getLow(), HistoryData.getOpen(),
                                HistoryData.getVolume(), HistoryData.getVolume_Cur(),
                                HistoryData.getBuySell_Ratio(),
                                HistoryData.getLastPrice()
                        );
                    }

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
                            HistoryData = new TickerHistoryData(HistoryData.getLastPurchaseTime(), HistoryData.getLastTradeId(), HistoryData.getLastPrice(), HistoryData.isCoinbase_CampBX());
                            break;
                        }
                        /*case DatabaseError: { // Not possible to be returned by 'tryCommitDatabase'.
                         }*/
                        case Time_Not_Ready: { // nth to do
                            break;
                        }
                    }
                }
            } finally {
                IsLoading = false;
            }
        }
    }

    private static final Object localStorageReadWriteMutex = new Object();

    public class TickerCacheTask_MSSql implements Runnable {

        private final String ExchangeCurrencyPair;
        private final String CurrencyPair_;
        private final String ExchangeSite;
        private long LastCachedTime = 0;
        private boolean IsLoading = false, IsFirstDataAcquisition = true;

        // Switching between MSSQL and data from other peers
        private LoggingSaveRunnable runnable = null;
        private boolean isDataAcquisitionFromMSSQL_Completed = false; // just for reference, not using it for now

        public TickerCacheTask_MSSql(String ExchangeCurrencyPair, String ExchangeSite, String CacheCurrencyPair) {
            this.ExchangeCurrencyPair = ExchangeCurrencyPair;
            this.CurrencyPair_ = CacheCurrencyPair;
            this.ExchangeSite = ExchangeSite;
        }

        /**
         * Set this reference so we are able to cancel this task later when we
         * dont need the database anymore
         *
         * @param runnable the LoggingSaveRunnable returned by the Timer task.
         * @see LoggingSaveRunnable
         */
        public void setLoggingSaveRunnable(LoggingSaveRunnable runnable) {
            this.runnable = runnable;
        }

        /**
         * A variable to determine if we still need to cache new items from the
         * database, otherwise it'll be reliant on the peer server
         *
         * @return boolean true/false
         */
        public boolean isDataAcquisitionFromMSSQL_Completed() {
            return isDataAcquisitionFromMSSQL_Completed;
        }

        @Override
        public void run() {
            if (isDataAcquisitionFromMSSQL_Completed || IsLoading) {
                return;
            }
            IsLoading = true;

            final List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);

            // First acquire from storage
            final List<TickerItemData> list_newItems_storage = new ArrayList(); // create a new array first and replace later
            if (IsFirstDataAcquisition) {
                System.out.println("Caching currency pair from Storage: " + ExchangeCurrencyPair);

                ReadFromFileStorage(list_newItems_storage);

                for (TickerItemData cur : list_newItems_storage) {
                    currentList.add(cur);
                }

                IsFirstDataAcquisition = false;
            }

            // Try SQL
            System.out.println("Caching currency pair from SQLserv: " + ExchangeCurrencyPair);

            final List<TickerItemData> list_newItems = new ArrayList(); // create a new array first and replace later
            
            final long biggest_server_time_result = MicrosoftAzureDatabaseExt.selectGraphData(ExchangeSite, CurrencyPair_, 60000, 24, LastCachedTime, list_newItems);
            if (biggest_server_time_result != -2) { // is not an error
                // Set max server_time
                if (biggest_server_time_result > LastCachedTime) {
                    LastCachedTime = biggest_server_time_result;
                }
                for (TickerItemData cur : list_newItems) {
                    currentList.add(cur);
                }

                System.out.println("[Info] Caching price for " + ExchangeCurrencyPair + " --> " + list_newItems.size() + ", MaxServerTime:" + LastCachedTime);

                // if SQL says fine/done, then done :D its the absolute thing...
                if (list_newItems.size() <= 100 || biggest_server_time_result == -2) { // Are we done caching yet?
                    completedCaching(ExchangeCurrencyPair);
                }
            }
            IsLoading = false;
        }

        private void completedCaching(String ExchangeCurrencyPair) {
            isDataAcquisitionFromMSSQL_Completed = true;
            canAcceptNewInfoFromOtherPeers.add(ExchangeCurrencyPair.hashCode());
            runnable.getSchedule().cancel(false); // cancel this cache task completely.

            commitToFileStorage();

            System.out.println("[Info] Stopped caching " + ExchangeCurrencyPair + " data from MSSQL");
        }

        private void ReadFromFileStorage(List<TickerItemData> list_newItems) {
            // Start saving to local file
            File f = new File("CachedPrice");
            if (!f.exists()) {
                f.mkdirs();
            }
            try {
                File f_data = new File(f, ExchangeCurrencyPair);
                if (!f_data.exists()) {
                    return;
                }
                synchronized (localStorageReadWriteMutex) { // not enough memory to run everything concurrently 
                    FileInputStream fis = null;
                    BufferedReader reader = null;
                    try {
                        fis = new FileInputStream(f_data);

                        byte[] byte_line = new byte[fis.available()];
                        int readbytes = fis.read(byte_line, 0, fis.available());
                        if (readbytes != -1) {
                            final SeekableLittleEndianAccessor slea = new GenericSeekableLittleEndianAccessor(new ByteArrayByteStream(byte_line));

                            int data_size = slea.readInt();
                            for (int i = 0; i < data_size; i++) {
                                final byte startingMarker = slea.readByte();

                                // starting and ending marker to ensure simple checksum and the file integrity
                                // if those markers are not -1, reload the entire one from Microsoft SQL database
                                if (startingMarker != -1) {
                                    // cleanup
                                    list_newItems.clear();
                                    return;
                                }
                                float close = slea.readFloat();
                                float open = slea.readFloat();
                                float high = slea.readFloat();
                                float low = slea.readFloat();
                                long servertime = slea.readLong();
                                double volume = slea.readDouble();
                                double volume_cur = slea.readDouble();
                                float ratio = slea.readFloat();

                                TickerItemData data = new TickerItemData(servertime, close, high, low, open, volume, volume_cur, ratio, false);
                                list_newItems.add(data);

                                // update max server time
                                if (servertime > LastCachedTime) {
                                    LastCachedTime = servertime;
                                }
                            }
                        }
                    } catch (Exception error) {
                        // data is corrupted?
                        error.printStackTrace();
                        f_data.delete();

                        list_newItems.clear();
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                        if (fis != null) {
                            fis.close();
                        }
                    }
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }

        private void commitToFileStorage() {
            // Start saving to local file
            File f = new File("CachedPrice");
            if (!f.exists()) {
                f.mkdirs();
            }
            try {
                File f_data = new File(f, ExchangeCurrencyPair + ".temp");
                f_data.createNewFile();

                File f_target = new File(f, ExchangeCurrencyPair);

                synchronized (localStorageReadWriteMutex) { // not enough memory to run everything concurrently 
                    // Create a new ArrayList here to prevent threading issue
                    // create here so we dont create unnecessary memory allocation until this synchronized block is called
                    List<TickerItemData> currentList = new ArrayList(list_mssql.get(ExchangeCurrencyPair));

                    // override existing file if any.
                    FileOutputStream out = null;
                    try {
                        out = new FileOutputStream(f_data, false);

                        PacketLittleEndianWriter mplew = new PacketLittleEndianWriter();
                        // Write packet data size
                        mplew.writeInt(currentList.size());

                        final byte[] dataWrite = mplew.getPacket();
                        out.write(dataWrite, 0, dataWrite.length);

                        // Loop through the data and write for each individual entry
                        for (TickerItemData data : currentList) {
                            PacketLittleEndianWriter plew2 = new PacketLittleEndianWriter(); // using multiple mplews to assist garbage collection
                            plew2.write(-1); // starting marker

                            plew2.writeFloat(data.getClose());
                            plew2.writeFloat(data.getOpen());
                            plew2.writeFloat(data.getHigh());
                            plew2.writeFloat(data.getLow());
                            plew2.writeLong(data.getServerTime());
                            plew2.writeDouble(data.getVol());
                            plew2.writeDouble(data.getVol_Cur());
                            plew2.writeFloat(data.getBuySell_Ratio());

                            final byte[] dataWrite2 = plew2.getPacket();
                            out.write(dataWrite2, 0, dataWrite2.length);
                        }
                    } catch (Exception error) {
                        error.printStackTrace();

                        // data is corrupted?
                        f_data.delete();
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                    }
                    if (f_target.exists()) {
                        f_target.delete();
                    }
                    Files.move(f_data.toPath(), f_target.toPath());
                    f_data.delete();
                }
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }

    /**
     * When receiving new graph data from other peers on other server
     *
     * @param ExchangeCurrencyPair The exchange + currency pair: eg:
     * btce-btc_usd
     * @param server_time the server time in millis
     * @param close the closing price
     * @param high the highest possible price
     * @param low the lowest possible price
     * @param open the opening price
     * @param volume the volume
     * @param volume_cur the volume detonated in the primary currency, eg
     * @param buysell_ratio bitcoin instead of USD
     */
    public void receivedNewGraphEntry_OtherPeers(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio) {
        //System.out.println(String.format("[Info] New info from other peers %s [%d], Close: %f, High: %f", ExchangeCurrencyPair, server_time, close, high));

        if (list_mssql.containsKey(ExchangeCurrencyPair)
                && canAcceptNewInfoFromOtherPeers.contains(ExchangeCurrencyPair.hashCode())) { // First item, no sync needed
            List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);

            synchronized (currentList) {
                if (currentList.size() > 0) {
                    TickerItemData lastItem = currentList.get(currentList.size() - 1);

                    // If an unmatured data is available
                    // replace it with a matured ones
                    if (lastItem.isUnmaturedData()) {
                        lastItem.replaceUnmaturedData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, false);
                        return;
                    }
                }
                // add new item entry
                currentList.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, false));
                //System.out.println("[Info] Added New info from other peers");
            }
        }
    }

    public void recievedNewUnmaturedData(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float lastprice) {
        if (list_mssql.containsKey(ExchangeCurrencyPair)
                && canAcceptNewInfoFromOtherPeers.contains(ExchangeCurrencyPair.hashCode())) { // First item, no sync needed
            List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);

            synchronized (currentList) {
                if (currentList.size() > 0) {
                    TickerItemData lastItem = currentList.get(currentList.size() - 1);

                    // replace or add new unmatured data
                    if (lastItem.isUnmaturedData()) {
                        lastItem.replaceUnmaturedData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true);
                    } else {
                        currentList.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true));
                    }
                }
            }
        }
    }
}
