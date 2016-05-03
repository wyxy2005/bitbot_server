package bitbot.cache.swaps;

import bitbot.cache.swaps.HTTP.Swaps_Bitfinex;
import bitbot.cache.tickers.TickerItemData;
import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.util.database.MicrosoftAzureDatabaseExt;
import bitbot.handler.channel.ChannelServer;
import bitbot.logging.ServerLog;
import bitbot.logging.ServerLogType;
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
public class SwapsCacheTask {

    // MSSQL
    private static final int MSSQL_CacheRefreshTime_Seconds = 60;

    // Acquiring of old data from mssql database
    private final List<Integer> canAcceptNewInfoFromOtherPeers = new LinkedList(); // string = exchange+currency pair
    private final Map<String, List<SwapsItemData>> list_mssql;
    private final List<LoggingSaveRunnable> runnable_mssql = new ArrayList();

    // Acquiring of data directly from the trades
    private final List<LoggingSaveRunnable> runnable_exchangeHistory = new ArrayList();

    public SwapsCacheTask() {
        this.list_mssql = new LinkedHashMap<>();

        StartScheduleTask();
    }

    public void StartScheduleTask() {
        for (String ExchangeCurrency : ChannelServer.getInstance().getCachingSwapCurrencies()) {
            final String[] Source_pair = ExchangeCurrency.split("-");
            final String ExchangeSite = Source_pair[0];
            final String Currency = Source_pair[1];

            // graph fetching from database
            if (ChannelServer.getInstance().isEnableSwapsSQLDataAcquisition()) {
                SwapsCacheTask_MSSql tickercache = new SwapsCacheTask_MSSql(ExchangeSite, Currency, ExchangeCurrency);
                LoggingSaveRunnable runnable = TimerManager.register(tickercache, MSSQL_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE);

                // set this reference so we are able to cancel this task later when we dont need the database anymore
                tickercache.setLoggingSaveRunnable(runnable);

                runnable_mssql.add(runnable);

                List<SwapsItemData> arrays = new ArrayList(); // same reference
                list_mssql.put(ExchangeCurrency, arrays);
            }

            // History
            if (ChannelServer.getInstance().isEnableEnableSwaps()) {
                SwapsInterface swap = null;
                int DefaultUpdateTime = 15; // 5 minutes

                if (ExchangeSite.contains("bitfinex")) {
                    swap = new Swaps_Bitfinex();
                }

                if (swap != null) {
                    runnable_exchangeHistory.add(TimerManager.register(
                            new SwapsCacheTask_ExchangeHistory(ExchangeSite, Currency, ExchangeCurrency, swap),
                            DefaultUpdateTime * 60000,
                            60000, // wait 60 seconds to start, as the spot_price might not been indexed on the world server
                            Integer.MAX_VALUE));
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

    public List<List<SwapsItemData>> getSwapsData(String Exchange, String currencies, long ServerTimeFrom_, final int numPastCandlesToReturn, int timeframe) {
        final List<List<SwapsItemData>> list_chart = new ArrayList(); // create a new array first

        // currencies seperated by '-'
        final String[] currenciesArray = currencies.split("-");

        final long cTime = (System.currentTimeMillis() / 1000l);
        final long startTime = truncateTimeToRound(timeframe, cTime - ((numPastCandlesToReturn * timeframe) * 60));

        final long ServerTimeFrom;
        if (ServerTimeFrom_ == 0) {
            ServerTimeFrom = startTime;
        } else {
            ServerTimeFrom = ServerTimeFrom_;
        }

        for (String currency : currenciesArray) {
            final String dataSet = String.format("%s-%s", Exchange, currency);

            // Is the data set available?
            if (!list_mssql.containsKey(dataSet)) {
                return list_chart;
            }
            final List<SwapsItemData> currentList = list_mssql.get(dataSet);
            final Stream<SwapsItemData> items_stream;
            synchronized (currentList) {
                items_stream = currentList.stream().
                        filter((data) -> (data.getTimestamp() > ServerTimeFrom)).
                        sorted(TickerItemComparator);

                final List<SwapsItemData> newArray = new ArrayList<>();
                list_chart.add(newArray);

                getSwapsDataInternal(items_stream, startTime, timeframe, newArray);
            }
        }
        return list_chart;
    }

    private void getSwapsDataInternal(Stream<SwapsItemData> items_stream, final long startTime, int intervalMinutes, List<SwapsItemData> outputData) {
        // Timestamp
        long LastUsedTime = 0;

        float rate = 0, spot_price = 0;
        double amount_lent = 0;

        boolean processedFirstTime = false;

        final int interval_seconds = intervalMinutes * 60;

        // Now we create the candle data chain
        final Iterator<SwapsItemData> items = items_stream.iterator();
        while (items.hasNext()) {
            // Loop through the entire data chain, which each data representing 1 minute of the snapshot.
            // However if that minute does not have any trading activity, the data will not be present. We detect this missing data by its time.
            final SwapsItemData item = items.next();

            if (!processedFirstTime) {
                while (LastUsedTime < item.getTimestamp()) {
                    LastUsedTime += interval_seconds;
                }
                processedFirstTime = true;
            }

            // Real stuff here
            final long endCandleTime = LastUsedTime + (long) interval_seconds;

            // If the stored item time is above the supposed expected
            // end candle time 
            if (item.getTimestamp() > endCandleTime) {
                long endCandleTime2 = endCandleTime;

                boolean isEmptyBar = false;

                while (item.getTimestamp() > endCandleTime2) {
                    
                    final SwapsItemData item_ret;
                    if (!isEmptyBar) {
                        item_ret = new SwapsItemData(rate, spot_price, amount_lent, endCandleTime2);
                    } else {
                        item_ret = new SwapsItemData(rate, spot_price, amount_lent, endCandleTime2);
                    }
                    outputData.add(item_ret);

                    rate = item.getRate();
                    spot_price = item.getSpotPrice();
                    amount_lent = item.getAmountLent();

                    // Update the next time 
                    LastUsedTime = endCandleTime2;
                    endCandleTime2 = LastUsedTime + (long) interval_seconds;

                    // Reset emptybar data
                    isEmptyBar = true;
                }
                // Otherwise, create a new candle data
            } else { 
                // here is meant for combining high/low/volume data, but since its bitfinex swaps
                // nothing much for us to do
                
                rate = item.getRate();
                spot_price = item.getSpotPrice();
                amount_lent = item.getAmountLent();
            }
        }
    }

    /*public List<TickerItem_CandleBar> getSwapsDataInternal_TradingView(
     final String tickers,
     final int backtestHours,
     int intervalMinutes,
     String ExchangeSite,
     long ServerTimeFrom, // Epoch in seconds
     long ServerTimeEnd, // Epoch in seconds
     boolean returnExactRequestedFromAndToTime) {

     final List<TickerItem_CandleBar> list_chart = new ArrayList(); // create a new array first
     final String[] currenciesArray = tickers.split("-");

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

     // Get the first element and re-calibrate the time to be used 
     // to start processing the return data
     // Post process, for the variables and stuff
     // round the last used time to best possible time for the chart period
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
     LastUsedTime = DateUtils.truncate(dtCal, truncateField).getTimeInMillis();

     // Loop through all currency list
     final List<Object[]> listitems_swapsDataset = new ArrayList();
     for (String currency : currenciesArray) {
     final String dataSet = String.format("%s-%s", ExchangeSite, currency);

     // Is the data set available?
     if (!list_mssql.containsKey(dataSet)) {
     return list_chart;
     }

     final Object[] items_swapsDataset = list_mssql.get(dataSet).stream().
     filter((data) -> (data.getTimestamp() > ServerTimeFrom && data.getTimestamp() <= _ServerTimeEnd)).
     sorted(TickerItemComparator).toArray();

     listitems_swapsDataset.add(items_swapsDataset);
     }

     if (listitems_swapsDataset.isEmpty()) {
     return list_chart; // ERROR!
     }
        
     // Process all items in the list together
     // Always use the first dataset as reference 
     boolean processedFirstTime = false;

     // Now we create the candle data chain
     for (int i = 0; i < listitems_swapsDataset.get(0).length; i++) {
     // Loop through the entire data chain, which each data representing 1 minute of the snapshot.
     // However if that minute does not have any trading activity, the data will not be present. We detect this missing data by its time.
     final SwapsItemData item = (SwapsItemData) listitems_swapsDataset.get(0)[i];

     if (!processedFirstTime) {
     while (LastUsedTime < item.getTimestamp()) {
     LastUsedTime += intervalMinutes;
     }
     processedFirstTime = true;
     }

     // Real stuff here
     final long endCandleTime = LastUsedTime + (long) intervalMinutes;

     // If the stored item time is above the supposed expected
     // end candle time 
     if (item.getTimestamp() > endCandleTime) {
     long endCandleTime2 = endCandleTime;
     boolean isEmptyBar = false;

     while (item.getTimestamp() > endCandleTime2) {
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

     return list_chart;
     }*/
    private static final Comparator<Object> TickerItemComparator = (Object obj1, Object obj2) -> {
        SwapsItemData data1 = (SwapsItemData) obj1;
        SwapsItemData data2 = (SwapsItemData) obj2;

        if (data1.getTimestamp() > data2.getTimestamp()) {
            return 1;
        } else if (data1.getTimestamp() == data2.getTimestamp()) {
            return 0;
        }
        return -1;
    };

    public void receivedNewGraphEntry_OtherPeers(String ExchangeCurrency, float rate, float spot_price, double amount_lent, int timestamp) {
        System.out.println(String.format("[Info] New swap info from other peers %s [%d], Rate: %f, spot_price: %f, amount_lent: %f, timestamp: %d", ExchangeCurrency, timestamp, rate, spot_price, amount_lent, timestamp));

        if (list_mssql.containsKey(ExchangeCurrency)
                && canAcceptNewInfoFromOtherPeers.contains(ExchangeCurrency.hashCode())) { // First item, no sync needed
            final List<SwapsItemData> currentList = list_mssql.get(ExchangeCurrency);

            synchronized (currentList) {
                currentList.add(new SwapsItemData(rate, spot_price, amount_lent, timestamp));
            }
            //System.out.println("[Info] Added New info from other peers");
        }
    }

    public class SwapsCacheTask_ExchangeHistory implements Runnable {

        private boolean IsLoading;
        private final String ExchangeSite, Currency, ExchangeCurrency;

        private final SwapsInterface swapsObj;

        public SwapsCacheTask_ExchangeHistory(String ExchangeSite, String Currency, String ExchangeCurrency, SwapsInterface HistoryConnector) {
            this.Currency = Currency;
            this.ExchangeSite = ExchangeSite;
            this.ExchangeCurrency = ExchangeCurrency;
            this.IsLoading = false;
            this.swapsObj = HistoryConnector;
        }

        @Override
        public void run() {
            if (IsLoading) // prevent double-caching of data at that instance
            {
                return; // don't want to lock this thread anyway, if one is delayed so be it.
            }
            IsLoading = true;

            System.out.println(String.format("[Swaps] Updating: %s", ExchangeCurrency));

            try {
                SwapsHistoryData result = swapsObj.connectAndParseSwapsResult(ExchangeSite, Currency, ExchangeCurrency);

                if (result != null) {
                    // doesn't matter without an external thread scheduling
                    // since its once per minute.

                    // This runs on a different thread executor, also it writes to disk/cached memory if database commit fails and retries every 5 minutes
                    BacklogCommitTask_Swaps.RegisterForImmediateLogging(result);
                }
            } catch (Exception exp) {
                ServerLog.RegisterForLoggingException(ServerLogType.SwapTask, exp);
            } finally {
                IsLoading = false; // ensure that this always runs
            }
        }
    }

    private static final Object localStorageReadWriteMutex = new Object();

    public class SwapsCacheTask_MSSql implements Runnable {

        private final String Currency;
        private final String ExchangeSite;
        private final String ExchangeCurrency;
        private long LastCachedTime = 0;

        // Switching between MSSQL and data from other peers
        private LoggingSaveRunnable runnable = null;
        private boolean isDataAcquisitionFromMSSQL_Completed = false; // just for reference, not using it for now
        private boolean IsFirstDataAcquisition = true; // Determines if this is the first time its acquiring data... [load from local storage]
        private boolean IsLoading = false;

        public SwapsCacheTask_MSSql(String ExchangeSite, String Currency, String ExchangeCurrency) {
            this.Currency = Currency;
            this.ExchangeSite = ExchangeSite;
            this.ExchangeCurrency = ExchangeCurrency;
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

            System.out.println("[Info] Caching swaps from SQLserv: " + ExchangeSite + ":" + Currency);

            try {
                final List<SwapsItemData> currentList = list_mssql.get(ExchangeCurrency);
                final List<SwapsItemData> list_newItems_storage = new ArrayList(); // create a new array first and replace later

                // Load data from local storage
                if (IsFirstDataAcquisition) {
                    System.out.println("Caching swap data from storage: " + ExchangeCurrency);

                    ReadFromFileStorage(list_newItems_storage);

                    for (SwapsItemData cur : list_newItems_storage) {
                        currentList.add(cur);
                    }
                    IsFirstDataAcquisition = false;
                }

                final List<SwapsItemData> list_newItems = new ArrayList(); // create a new array first and replace later

                // Load data from sql server
                long biggest_server_time_result = MicrosoftAzureDatabaseExt.selectSwapsData(ExchangeSite, Currency, 60000, LastCachedTime, list_newItems);
                if (biggest_server_time_result != -1) { // temporary network issue or unavailable
                    // Set max server_time
                    if (biggest_server_time_result > LastCachedTime) {
                        LastCachedTime = biggest_server_time_result;
                    }
                    for (SwapsItemData cur : list_newItems) {
                        currentList.add(cur);
                    }

                    System.out.println("[Info] Caching swap data for " + ExchangeCurrency + " --> " + list_newItems.size() + ", MaxServerTime:" + LastCachedTime);

                    if (list_newItems.size() <= 100) { // Are we done caching yet?
                        completedCaching();
                    }
                }
            } catch (Exception exp) {
                ServerLog.RegisterForLoggingException(ServerLogType.SwapTask, exp);
            } finally {
                IsLoading = false;
            }
        }

        private void completedCaching() {
            isDataAcquisitionFromMSSQL_Completed = true;
            canAcceptNewInfoFromOtherPeers.add(ExchangeCurrency.hashCode());
            runnable.getSchedule().cancel(false); // cancel this cache task completely.

            commitToFileStorage();

            System.out.println("[Info] Stopped caching " + ExchangeCurrency + " swap data from MSSQL");
        }

        private void ReadFromFileStorage(List<SwapsItemData> list_newItems) {
            // Start saving to local file
            File f = new File("CachedSwaps");
            if (!f.exists()) {
                f.mkdirs();
            }
            try {
                File f_data = new File(f, ExchangeCurrency);
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
                                float rate = slea.readFloat();
                                float spot_price = slea.readFloat();
                                int timestamp = slea.readInt();
                                double amount = slea.readDouble();

                                SwapsItemData data = new SwapsItemData(rate, spot_price, amount, timestamp);
                                list_newItems.add(data);

                                // update max server time
                                if (timestamp > LastCachedTime) {
                                    LastCachedTime = timestamp;
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
            File f = new File("CachedSwaps");
            if (!f.exists()) {
                f.mkdirs();
            }
            try {
                File f_data = new File(f, ExchangeCurrency + ".temp");
                f_data.createNewFile();

                File f_target = new File(f, ExchangeCurrency);

                synchronized (localStorageReadWriteMutex) { // not enough memory to run everything concurrently 
                    // Create a new ArrayList here to prevent threading issue
                    // create here so we dont create unnecessary memory allocation until this synchronized block is called
                    List<SwapsItemData> currentList = new ArrayList(list_mssql.get(ExchangeCurrency));

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
                        for (SwapsItemData data : currentList) {
                            PacketLittleEndianWriter plew2 = new PacketLittleEndianWriter(); // using multiple mplews to assist garbage collection
                            plew2.write(-1); // starting marker

                            plew2.writeFloat(data.getRate());
                            plew2.writeFloat(data.getSpotPrice());
                            plew2.writeInt((int) data.getTimestamp());
                            plew2.writeDouble(data.getAmountLent());

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

}
