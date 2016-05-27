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
import bitbot.cache.tickers.HTTP.TickerHistory_FybSGSE;
import bitbot.cache.tickers.HTTP.TickerHistory_Bitstamp;
import bitbot.cache.tickers.HTTP.TickerHistory_Cryptsy;
import bitbot.cache.tickers.HTTP.TickerHistory_796;
import bitbot.cache.tickers.HTTP.TickerHistory_CoinbaseExchange;
import bitbot.util.database.MicrosoftAzureDatabaseExt;
import bitbot.handler.channel.ChannelServer;
import bitbot.Constants;
import bitbot.cache.tickers.HTTP.TickerHistory_BitVC;
import bitbot.cache.tickers.HTTP.TickerHistory_Gemini;
import bitbot.cache.tickers.HTTP.TickerHistory_Huobi;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import bitbot.util.encryption.input.ByteArrayByteStream;
import bitbot.util.encryption.input.GenericSeekableLittleEndianAccessor;
import bitbot.util.encryption.input.SeekableLittleEndianAccessor;
import bitbot.util.encryption.output.PacketLittleEndianWriter;
import bitbot.util.packets.ServerSocketExchangePacket;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
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
    private final List<LoggingSaveRunnable> runnable_mssql = new ArrayList();
    private final Map<String, List<TickerItemData>> list_mssql;
    private final Map<String, List<TickerItemData>> list_unmaturedData;

    // List of currencies eligible to show a 24 hour snapshot
    private static final List<String> list_currenciesAllowedFor24HSnapshot = new ArrayList();

    // Acquiring of data directly from the trades
    private final List<LoggingSaveRunnable> runnable_exchangeHistory = new ArrayList();

    public TickerCacheTask() {
        this.list_mssql = new LinkedHashMap<>();
        this.list_unmaturedData = new LinkedHashMap<>(); // LinkedHashMap to maintain order

        StartScheduleTask();
    }

    public void StartScheduleTask() {
        // List of currencies eligible to show a 24 hour snapshot 
        // Loading from Azure App Services
        final String[] currencies = MicrosoftAzureDatabaseExt.selectShowSummaryCurrencyPairs();
        for (String s : currencies) {
            list_currenciesAllowedFor24HSnapshot.add(s);
        }

        // List of currencies this instance server is to cache or to index, 
        // As defined under currencypairs.properties
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

                // Unmatured
                list_unmaturedData.put(ExchangeCurrencyPair, new ArrayList());
            }

            // History
            if (ChannelServer.getInstance().isEnableTickerHistory()) {
                TickerHistoryInterface history = null;
                int UpdateTime_Millis = 10000;

                final boolean trackLargeTrades = ChannelServer.getInstance().getCurrencyPairsForLargeTrades().contains(ExchangeCurrencyPair);

                if (ExchangeCurrencyPair.contains("huobi")) {
                    history = new TickerHistory_Huobi(trackLargeTrades);
                    UpdateTime_Millis = 500;

                    // history = new SocketTickerHistory_Huobi(trackLargeTrades, ExchangeSite, CurrencyPair);
                    // UpdateTime_Millis = 10000; // Check the socket state once every 10 seconds
                } else if (ExchangeCurrencyPair.contains("bitvc")) {
                    history = new TickerHistory_BitVC(trackLargeTrades);
                    UpdateTime_Millis = 5000;

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
                        UpdateTime_Millis = 3000;
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

                } else if (ExchangeCurrencyPair.contains("gemini")) {
                    history = new TickerHistory_Gemini(trackLargeTrades);
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
        synchronized (currentList) {
            return currentList.get(currentList.size() - 1);
        }
    }

    private static long truncateTimeToRound(int intervalMinutes, long LastUsedTime) {
        final Calendar dtCal = Calendar.getInstance();
        dtCal.setTimeInMillis(LastUsedTime);

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
        return DateUtils.truncate(dtCal, truncateField).getTimeInMillis();
    }

    public List<TickerItem_CandleBar> getTickerList_Candlestick(
            final String ticker,
            final int backtestHours,
            int intervalMinutes,
            String ExchangeSite,
            long ServerTimeFrom, // Epoch in seconds
            long ServerTimeEnd, // Epoch in seconds
            boolean returnExactRequestedFromAndToTime,
            boolean requestSummary) {

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

        // Etc
        final boolean isTradingViewData = ServerTimeFrom != 0;
        final int numCandlesReturned = (int) (ServerTimeEnd - ServerTimeFrom) / intervalMinutes;
        final boolean returnAllPossibleLatestData = requestSummary || (isTradingViewData && (numCandlesReturned > 100));

        //System.out.println("[" + intervalMinutes / 60 + "] Return all: " + returnAllPossibleLatestData + " " + numCandlesReturned);
        // truncate the ending time to max current time
        // This could also prevent DOS
        long timeDilusion = cTime - ServerTimeEnd;
        if (Math.abs(cTime - ServerTimeEnd) > 60 * 5) { // Time difference is 5 minutes, just use the server time. This could also prevent denial of service by sending long.maxvalue
            ServerTimeEnd = cTime_Millis;
            timeDilusion = 0;
        }
        final long _ServerTimeEnd = ServerTimeEnd;

        // Determine where to start the candlestick
        final long LastUsedTime_;
        long LastUsedTime = 0;
        if (isTradingViewData) { // tradingview
            LastUsedTime_ = ServerTimeFrom - intervalMinutes;
        } else {
            LastUsedTime_ = cTime - (60l * 60l * backtestHours);
        }
        LastUsedTime = LastUsedTime_;

        float high = 0, low = Float.MAX_VALUE, open = -1, lastPriceSet = 0, lastCloseSet = 0;
        double Volume = 0, VolumeCur = 0;
        float buysell_ratio_Total = 0, buysellratio_sets = 1;

        // Get the first element and re-calibrate the time to be used 
        // to start processing the return data
        // Post process, for the variables and stuff
        // round the last used time to best possible time for the chart period
        LastUsedTime = truncateTimeToRound(intervalMinutes, LastUsedTime);

        // Gets the list of data relevent to this request, sorted by time in ascending order
        final List<TickerItemData> currentList = list_mssql.get(dataSet);
        final Stream<TickerItemData> items_stream;
        synchronized (currentList) {
            items_stream = currentList.stream().
                    filter((data)
                            -> (data.getServerTime() >= LastUsedTime_ && (returnAllPossibleLatestData || data.getServerTime() <= _ServerTimeEnd))).
                    sorted(TickerItemComparator);         // No need to lock this thread, if we are creating a new ArrayList off existing since its a copy :)

            boolean processedFirstTime = false;

            // Uncompleted candle data
            boolean haveUncompletedCandle = false;
            long lastUncompletedCandleTime = 0;

            // Now we create the candle data chain
            final Iterator<TickerItemData> items = items_stream.iterator();
            while (items.hasNext()) {
                // Loop through the entire data chain, which each data representing 1 minute of the snapshot.
                // However if that minute does not have any trading activity, the data will not be present. We detect this missing data by its time.
                final TickerItemData item = items.next();

                if (!processedFirstTime) {
                    while (LastUsedTime < item.getServerTime()) {
                        LastUsedTime += intervalMinutes;
                    }
                    processedFirstTime = true;
                }

                // Real stuff here
                final long endCandleTime = LastUsedTime + (long) intervalMinutes;

                // If the stored item time is above the supposed expected
                // end candle time 
                if (item.getServerTime() > endCandleTime) {
                    long endCandleTime2 = endCandleTime;
                    boolean isEmptyBar = false;

                    while (item.getServerTime() > endCandleTime2) {
                        TickerItem_CandleBar item_ret;
                        if (!isEmptyBar) {
                            item_ret = new TickerItem_CandleBar(
                                    endCandleTime2,
                                    lastPriceSet == -1.0f ? item.getOpen() : lastPriceSet,
                                    high,
                                    low,
                                    open,
                                    Volume,
                                    VolumeCur,
                                    (buysell_ratio_Total == 0.0f ? 1.0f : buysell_ratio_Total) / buysellratio_sets,
                                    false);
                        } else {
                            item_ret = new TickerItem_CandleBar(
                                    endCandleTime2, // Time
                                    lastPriceSet, // Last
                                    lastPriceSet, // High
                                    lastPriceSet, // Low
                                    lastPriceSet, // Open
                                    0.0, // Volume
                                    0.0, // Volume cur
                                    1.0f, // Ratio
                                    false);
                        }
                        list_chart.add(item_ret);
                        haveUncompletedCandle = false;

                        high = item.getHigh();
                        low = item.getLow();
                        open = item.getOpen();

                        if (includeVolumeData) {
                            Volume = item.getVol();
                            VolumeCur = item.getVol_Cur();
                        }

                        LastUsedTime = endCandleTime2;
                        endCandleTime2 = LastUsedTime + (long) intervalMinutes;

                        // Reset emptybar data
                        isEmptyBar = true;
                        lastUncompletedCandleTime = item.getServerTime();
                    }

                    // Otherwise, create a new candle data
                } else {
                    high = Math.max(item.getHigh(), high);
                    low = Math.min(item.getLow(), low);
                    if (open == -1.0f) {
                        open = item.getOpen();
                    }
                    if (includeVolumeData) {
                        Volume += item.getVol();
                        VolumeCur += item.getVol_Cur();
                    }
                    buysell_ratio_Total += item.getBuySell_Ratio();
                    buysellratio_sets += 1.0f;

                    haveUncompletedCandle = true;
                }
                lastPriceSet = item.getClose();
            }
            if (haveUncompletedCandle && lastUncompletedCandleTime != 0) {
                list_chart.add(new TickerItem_CandleBar(
                        lastUncompletedCandleTime,
                        lastPriceSet,
                        high,
                        low,
                        open,
                        Volume,
                        VolumeCur,
                        (buysell_ratio_Total == 0.0f ? 1.0f : buysell_ratio_Total) / buysellratio_sets,
                        false));
            }
        }
        /*for (int i = 0; i < 100; ++i) {
         if (LastUsedTime > ServerTimeEnd) {
         break;
         }
         final long endCandleTime = Math.min(ServerTimeEnd, LastUsedTime + (long) intervalMinutes);
         final TickerItem_CandleBar item_ret_last
         = new TickerItem_CandleBar(
         endCandleTime,
         lastPriceSet,
         high,
         low,
         open,
         Volume,
         VolumeCur,
         (buysell_ratio_Total == 0.0f ? 1.0f : buysell_ratio_Total) / buysellratio_sets,
         false);
         list_chart.add(item_ret_last);

         high = lastPriceSet;
         low = lastPriceSet;
         open = lastPriceSet;
         Volume = 0.0;
         VolumeCur = 0.0;
         buysell_ratio_Total = 0.0f;
         LastUsedTime += (long) intervalMinutes;

         if (endCandleTime == ServerTimeEnd) {
         break;
         }
         }*/
 /*if (returnExactRequestedFromAndToTime) {
         boolean breakloop = false;
         while (!(LastUsedTime >= ServerTimeEnd || breakloop)) {
         long time;
         if ((LastUsedTime += (long) intervalMinutes) > ServerTimeEnd) {
         breakloop = true;
         }
         if ((time = ServerTimeEnd) > LastUsedTime) {
         time = LastUsedTime;
         }
         final TickerItem_CandleBar unmatured = new TickerItem_CandleBar(time, lastPriceSet, lastPriceSet, lastPriceSet, lastPriceSet, 0.0, 0.0, 1.0f, true);
         list_chart.add(unmatured);
         }
         }*/
        return list_chart;
    }

    public List<TickerItem_CandleBar> GetTickerList_CumulativeVolume(
            final String ticker,
            final int backtestHours,
            int intervalMinutes,
            String ExchangeSite,
            long ServerTimeFrom, // Epoch in seconds
            long ServerTimeEnd, // Epoch in seconds
            boolean returnExactRequestedFromAndToTime) {

        final List<TickerItem_CandleBar> list_chart = new ArrayList(); // create a new array first
        final String dataSet = ExchangeSite + "-" + ticker;

        // Is the data set available?
        if (!list_mssql.containsKey(dataSet)) {
            return list_chart;
        }

        // Timestamp
        final long cTime_Millis = System.currentTimeMillis();
        intervalMinutes *= 60; // convert to seconds

        final long cTime = cTime_Millis / 1000;

        // Etc
        final boolean isTradingViewData = ServerTimeFrom != 0;
        final boolean isLastCandleRequest = ServerTimeEnd - ServerTimeFrom == intervalMinutes;

        // truncate the ending time to max current time
        // This could also prevent DOS
        long timeDilusion = cTime - ServerTimeEnd;
        if (Math.abs(cTime - ServerTimeEnd) > 60 * 5) { // Time difference is 5 minutes, just use the server time. This could also prevent denial of service by sending long.maxvalue
            ServerTimeEnd = cTime_Millis;
            timeDilusion = 0;
        }
        final long _ServerTimeEnd = ServerTimeEnd;

        // Determine where to start the candlestick
        final long LastUsedTime_;
        long LastUsedTime = 0;
        if (isTradingViewData) { // tradingview
            LastUsedTime_ = ServerTimeFrom - intervalMinutes;
        } else {
            LastUsedTime_ = cTime - (60l * 60l * backtestHours);
        }
        LastUsedTime = LastUsedTime_;

        float lastPriceSet = 0;
        float buysell_ratio_Total = 0, buysellratio_sets = 1;
        float CumulativeVolume = 0;

        // Get the first element and re-calibrate the time to be used 
        // to start processing the return data
        // Post process, for the variables and stuff
        // round the last used time to best possible time for the chart period
        LastUsedTime = truncateTimeToRound(intervalMinutes, LastUsedTime);

        // Gets the list of data relevent to this request, sorted by time in ascending order
        final List<TickerItemData> currentList = list_mssql.get(dataSet);
        final Stream<TickerItemData> items_stream;
        synchronized (currentList) {
            items_stream = currentList.stream().
                    filter((data)
                            -> (data.getServerTime() >= LastUsedTime_ && data.getServerTime() <= _ServerTimeEnd)).
                    sorted(TickerItemComparator);         // No need to lock this thread, if we are creating a new ArrayList off existing since its a copy :)

            boolean processedFirstTime = false;

            // Now we create the candle data chain
            final Iterator<TickerItemData> items = items_stream.iterator();
            while (items.hasNext()) {
                // Loop through the entire data chain, which each data representing 1 minute of the snapshot.
                // However if that minute does not have any trading activity, the data will not be present. We detect this missing data by its time.
                final TickerItemData item = items.next();

                if (!processedFirstTime) {
                    while (LastUsedTime < item.getServerTime()) {
                        LastUsedTime += intervalMinutes;
                    }
                    processedFirstTime = true;
                }

                // Real stuff here
                final long endCandleTime = LastUsedTime + (long) intervalMinutes;

                if (item.getBuySell_Ratio() != 0f && item.getBuySell_Ratio() != 1.0f) {
                    float buyAndSellRatio = item.getBuySell_Ratio() + 1.0f;

                    // volume cur
                    double buyVolumeCur = (item.getVol_Cur() / buyAndSellRatio) * item.getBuySell_Ratio();
                    double sellVolumeCur = item.getVol_Cur() - buyVolumeCur;

                    CumulativeVolume += buyVolumeCur;
                    CumulativeVolume -= sellVolumeCur;
                }

                // If the stored item time is above the supposed expected
                // end candle time 
                if (item.getServerTime() > endCandleTime) {
                    long endCandleTime2 = endCandleTime;
                    boolean isEmptyBar = false;

                    while (item.getServerTime() > endCandleTime2) {
                        TickerItem_CandleBar item_ret;
                        if (!isEmptyBar) {
                            item_ret = new TickerItem_CandleBar(
                                    endCandleTime2,
                                    CumulativeVolume, CumulativeVolume, CumulativeVolume, CumulativeVolume,
                                    0,
                                    0,
                                    (buysell_ratio_Total == 0.0f ? 1.0f : buysell_ratio_Total) / buysellratio_sets,
                                    false);
                        } else {
                            item_ret = new TickerItem_CandleBar(
                                    endCandleTime2, // Time
                                    CumulativeVolume, // Last
                                    CumulativeVolume, // High
                                    CumulativeVolume, // Low
                                    CumulativeVolume, // Open
                                    0.0, // Volume
                                    0.0, // Volume cur
                                    1.0f, // Ratio
                                    false);
                        }
                        list_chart.add(item_ret);

                        LastUsedTime = endCandleTime2;
                        endCandleTime2 = LastUsedTime + (long) intervalMinutes;

                        // Reset emptybar data
                        isEmptyBar = true;
                    }

                    // Otherwise, create a new candle data
                } else {
                    buysell_ratio_Total += item.getBuySell_Ratio();
                    buysellratio_sets += 1.0f;
                }
                lastPriceSet = item.getClose();
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

        // Get the first element and re-calibrate the time to be used 
        // to start processing the return data
        // Post process, for the variables and stuff
        // round the last used time to best possible time for the chart period
        long LastUsedTime = truncateTimeToRound(60, startTime);

        double totalBuyVolume_Cur = 0, totalSellVolume_Cur = 0;
        double totalBuyVolume = 0, totalSellVolume = 0;

        final List<TickerItemData> currentList = list_mssql.get(dataSet);

        //System.out.println("Start time: " + startTime + " , Cur time: " + cTime + " , " + currentList.get(currentList.size() - 1).getServerTime());
        final Iterator<TickerItemData> items;
        synchronized (currentList) {
            items = currentList.stream().
                    filter((data) -> (data.getServerTime() >= startTime)).
                    sorted(TickerItemComparator).
                    iterator();

            while (items.hasNext()) {
                final TickerItemData item = items.next();
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
        final Iterator<TickerItemData> items;
        synchronized (currentList) {
            items = currentList.stream().
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

        for (Entry<String, List<TickerItemData>> currentList : list_mssql.entrySet()) {
            synchronized (currentList) {
                mapPriceSummary.put(currentList.getKey(),
                        currentList.getValue().size() > 0 ? currentList.getValue().get(currentList.getValue().size() - 1) : null);
            }
        }
        return mapPriceSummary;
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

    public List<String> getlistCurrenciesAllowedFor24HSnapshot() {
        return list_currenciesAllowedFor24HSnapshot;
    }

    private static final TimeZone utc = TimeZone.getTimeZone("UTC");

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

        public String getExchangeSite() {
            return ExchangeSite;
        }

        public String getCurrencyPair() {
            return CurrencyPair;
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

            if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
                System.out.println(String.format("[TH] Updating price: %s", ExchangeCurrencyPair));
            }

            try {
                final TickerHistoryData data = HistoryConnector.connectAndParseHistoryResult(
                        this,
                        ExchangeCurrencyPair,
                        ExchangeSite,
                        CurrencyPair,
                        HistoryData != null ? HistoryData.getLastPurchaseTime() : 0, // Read from buy/sell history
                        HistoryData != null ? HistoryData.getLastTradeId() : 0);

                if (data != null) { // Network unavailable?
                    // Set data to UTC time.
                    // This UTC time is used for inserting to database
                    Calendar cal_UTC = Calendar.getInstance(utc);
                    data.setLastServerUTCTime(cal_UTC.getTimeInMillis());

                    if (HistoryData != null) {
                        HistoryData.merge(data); // Merge high + lows, volume and set last date where it is read
                    } else {
                        HistoryData = data;
                    }
                    final HistoryDatabaseCommitEnum commitResult = HistoryData.tryCommitDatabase(LastCommitTime, ExchangeCurrencyPair, readyToBroadcastPriceChanges());

                    switch (commitResult) {
                        case Ok: {
                            // DEBUG
                            if (ChannelServer.getInstance().isEnableDebugSessionPrints()) {
                                Calendar cal = Calendar.getInstance();
                                cal.setTimeInMillis(HistoryData.getLastServerUTCTime());
                                cal.set(Calendar.SECOND, 0);

                                System.out.println(String.format("[TH] %s Commited data hh:mm = (%s), High: %f, Low: %f, Volume: %f, VolumeCur: %f",
                                        ExchangeCurrencyPair, cal.getTime().toString(), HistoryData.getHigh(), HistoryData.getLow(), HistoryData.getVolume(), HistoryData.getVolume_Cur()));
                            }

                            LastCommitTime = HistoryData.getLastPurchaseTime();

                            // Set new
                            HistoryData = new TickerHistoryData(this, HistoryData.getLastPurchaseTime(), HistoryData.getLastTradeId(), HistoryData.getLastPrice(), HistoryData.isCoinbase_CampBX());
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

                synchronized (currentList) {
                    for (TickerItemData cur : list_newItems_storage) {
                        currentList.add(cur);
                    }
                }

                IsFirstDataAcquisition = false;
            }

            // Try SQL
            System.out.println("Caching currency pair from SQLserv: " + ExchangeCurrencyPair);

            final List<TickerItemData> list_newItems = new ArrayList(); // create a new array first and replace later

            final long biggest_server_time_result = MicrosoftAzureDatabaseExt.selectGraphData(ExchangeSite, CurrencyPair_, 60000, LastCachedTime, list_newItems);
            if (biggest_server_time_result != -2) { // is not an error
                // Set max server_time
                if (biggest_server_time_result > LastCachedTime) {
                    LastCachedTime = biggest_server_time_result;
                }
                synchronized (currentList) {
                    for (TickerItemData cur : list_newItems) {
                        currentList.add(cur);
                    }
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
            runnable.getSchedule().cancel(false); // cancel this cache task completely.

            commitToFileStorage();

            System.out.println("[Info] Stopped caching " + ExchangeCurrencyPair + " data from MSSQL");
        }

        private static final int MAX_TickerItem_PerFile = 400000;
        private static final int FILE_VERSIONING = 2;

        private void ReadFromFileStorage(List<TickerItemData> list_newItems) {
            // Start saving to local file
            File f = new File("CachedPrice");
            if (!f.exists()) {
                f.mkdirs();
            }
            try {
                // Loop through all possible files, there may be multiple for a single pair
                // Ex: bitfinex-btc_usd, bitfinex-btc_usd_1, bitfinex-btc_usd_2, bitfinex-btc_usd_3
                for (int z_fileCount = 0; z_fileCount < Integer.MAX_VALUE; z_fileCount++) {

                    final File f_data = new File(f, z_fileCount == 0 ? ExchangeCurrencyPair : (ExchangeCurrencyPair + "_" + z_fileCount));
                    if (!f_data.exists()) {
                        break;
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

                                int file_version = slea.readInt(); // to be used for future updates
                                int data_size = slea.readInt();
                                //System.out.println("[" + z_fileCount + "] Read count: " + data_size);

                                for (int i = 0; i < data_size; i++) {
                                    final byte startingMarker = slea.readByte();
                                    // starting and ending marker to ensure simple checksum and the file integrity
                                    // if those markers are not -1, reload the entire one from backup
                                    if (startingMarker != -1) {
                                        // cleanup
                                        list_newItems.clear();
                                        return;
                                    }
                                    final boolean isMaturedData = slea.readByte() > 0;

                                    if (isMaturedData) {
                                        float close = slea.readFloat();
                                        float open = slea.readFloat();
                                        float high = slea.readFloat();
                                        float low = slea.readFloat();
                                        long servertime = slea.readLong();
                                        double volume = slea.readDouble();
                                        double volume_cur = slea.readDouble();
                                        float ratio = slea.readFloat();

                                        final TickerItemData data = new TickerItemData(servertime, close, high, low, open, volume, volume_cur, ratio, false);
                                        list_newItems.add(data);

                                        // update max server time
                                        if (servertime > LastCachedTime) {
                                            LastCachedTime = servertime;
                                        }
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

                synchronized (localStorageReadWriteMutex) { // not enough memory to run everything concurrently 
                    // Create a new ArrayList here to prevent threading issue
                    // create here so we dont create unnecessary memory allocation until this synchronized block is called
                    final List<TickerItemData> currentList = new ArrayList(list_mssql.get(ExchangeCurrencyPair));
                    int itemWrittenCountTotal = 0;
                    int itemWrittenCount_File = 0;

                    // Loop through all possible files, there may be multiple for a single pair
                    // Ex: bitfinex-btc_usd, bitfinex-btc_usd_1, bitfinex-btc_usd_2, bitfinex-btc_usd_3
                    for (int z_fileCount = 0; z_fileCount < Integer.MAX_VALUE; z_fileCount++) {
                        final String storingFileName = z_fileCount == 0 ? ExchangeCurrencyPair : (ExchangeCurrencyPair + "_" + z_fileCount);

                        // Creates a temporary file, to replace existing one only if the write is done to prevent unfinished writing errors.
                        File f_data = new File(f, storingFileName + ".temp");
                        f_data.createNewFile();

                        File f_target = new File(f, storingFileName);
                        // override existing file if any.
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream(f_data, false);

                            final PacketLittleEndianWriter plew = new PacketLittleEndianWriter();
                            plew.writeInt(FILE_VERSIONING);

                            // Write packet data size
                            plew.writeInt(currentList.isEmpty()
                                    ? 0 : Math.min(MAX_TickerItem_PerFile, currentList.size() - (z_fileCount * MAX_TickerItem_PerFile) - 1));

                            //System.out.println("[" + z_fileCount + "] Dumping count: " + Math.min(MAX_TickerItem_PerFile, currentList.size() - (z_fileCount * MAX_TickerItem_PerFile)));
                            final byte[] dataWrite = plew.getPacket();
                            out.write(dataWrite, 0, dataWrite.length);

                            // Loop through the data and write for each individual entry
                            while (itemWrittenCount_File < MAX_TickerItem_PerFile && itemWrittenCountTotal < currentList.size()) {
                                final TickerItemData data = currentList.get(itemWrittenCountTotal);

                                final PacketLittleEndianWriter plew2 = new PacketLittleEndianWriter(); // using multiple mplews to assist garbage collection
                                plew2.write(-1); // starting marker

                                if (data.isUnmaturedData()) { // Only write matured data
                                    plew2.write(0);
                                } else {
                                    plew2.write(1);
                                    plew2.writeFloat(data.getClose());
                                    plew2.writeFloat(data.getOpen());
                                    plew2.writeFloat(data.getHigh());
                                    plew2.writeFloat(data.getLow());
                                    plew2.writeLong(data.getServerTime());
                                    plew2.writeDouble(data.getVol());
                                    plew2.writeDouble(data.getVol_Cur());
                                    plew2.writeFloat(data.getBuySell_Ratio());
                                }

                                final byte[] dataWrite2 = plew2.getPacket();
                                out.write(dataWrite2, 0, dataWrite2.length);

                                // Increment value of the number of items written.
                                itemWrittenCountTotal++;
                                itemWrittenCount_File++;

                                if (itemWrittenCount_File >= MAX_TickerItem_PerFile || itemWrittenCountTotal >= currentList.size()) { // 60,000 items per file.
                                    //System.out.println("Written: " + itemWrittenCount_File + " total: " + itemWrittenCountTotal);
                                    break;
                                }
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

                        if (itemWrittenCountTotal >= currentList.size()) {
                            // Delete any extra files lying around.
                            for (int z_fileCount2 = z_fileCount + 1; z_fileCount2 < Integer.MAX_VALUE; z_fileCount2++) {
                                final String storingFileName2 = z_fileCount2 == 0 ? ExchangeCurrencyPair : (ExchangeCurrencyPair + "_" + z_fileCount2);

                                File f_target2 = new File(f, storingFileName2);

                                if (f_target2.exists()) {
                                    f_target2.delete();
                                } else {
                                    break;
                                }
                            }
                            break;
                        }
                        itemWrittenCount_File = 0; // Reset
                    }
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

        if (list_mssql.containsKey(ExchangeCurrencyPair)) { // First item, no sync needed
            final List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);
            final List<TickerItemData> currentList_Unmatured = list_unmaturedData.get(ExchangeCurrencyPair);

            if (currentList == null || currentList_Unmatured == null) {
                return;
            }

            // Temporary rollback, due to upper limits of memory until a better solution is found.
            /*synchronized (currentList) {
             // Remove current unmatured data in existence
             final Stream<TickerItemData> items_unmatured = currentList.stream().filter(data -> data.isUnmaturedData());

             final Object[] items = items_unmatured.toArray();
             for (Object o : items) { // Don't use iterator here to prevent concurrent modification issue
             TickerItemData item = (TickerItemData) o;

             currentList.remove(item);
             }

             // Add the new candle data
             currentList.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, false));
             }*/
            synchronized (currentList) {
                // Add the new candle data
                currentList.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, false));
            }
            synchronized (currentList_Unmatured) {
                currentList_Unmatured.clear();
            }
        }

        // Broadcast
        if (ChannelServer.getInstance().isEnableSocketStreaming()
                && ChannelServer.getInstance().getServerSocketExchangeHandler() != null) {
            ChannelServer.getInstance().getServerSocketExchangeHandler().broadcastMessage(ServerSocketExchangePacket.getMinuteChanges(ExchangeCurrencyPair, server_time, close, high, low, open, volume, volume_cur, buysell_ratio));
        }
    }

    public void recievedNewUnmaturedData(String ExchangeCurrencyPair, long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, float lastprice) {
        if (list_unmaturedData.containsKey(ExchangeCurrencyPair)) { // First item, no sync needed
            final List<TickerItemData> currentList_Unmatured = list_unmaturedData.get(ExchangeCurrencyPair);
            if (currentList_Unmatured == null || currentList_Unmatured.isEmpty()) {
                return;
            }
            // Temporary rollback, due to upper limits of memory until a better solution is found.
            /* synchronized (currentList) {
             currentList.add(new TickerItemData(
             server_time, 
             close, 
             high, 
             low, 
             open, 
             volume, 
             volume_cur, 
             buysell_ratio, 
             true));
             }*/
            synchronized (currentList_Unmatured) {
                if (currentList_Unmatured.size() >= 1) {
                    final TickerItemData lastItem = currentList_Unmatured.get(currentList_Unmatured.size() - 1);
                    if (lastItem.isUnmaturedData()) {
                        lastItem.replaceUnmaturedData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true);
                    } else {
                        currentList_Unmatured.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true));
                    }
                } else {
                    currentList_Unmatured.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true));
                }
            }

            /*final List<TickerItemData> currentList = list_mssql.get(ExchangeCurrencyPair);

            synchronized (currentList) {
                // Add the new candle data
                currentList.add(new TickerItemData(server_time, close, high, low, open, volume, volume_cur, buysell_ratio, true));
            }*/
        }
    }
}
