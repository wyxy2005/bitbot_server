package bitbot.cache.swaps;

import bitbot.cache.swaps.HTTP.Swaps_Bitfinex;
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

    public List<List<SwapsItemData>> getSwapsData(String Exchange, String currencies, long ServerTimeFrom, final int backtestHours, int intervalMinutes) {
        final List<List<SwapsItemData>> list_chart = new ArrayList(); // create a new array first

        // currencies seperated by '-'
        String[] currenciesArray = currencies.split("-");

        for (String currency : currenciesArray) {
            final String dataSet = String.format("%s-%s", Exchange, currency);

            // Is the data set available?
            if (!list_mssql.containsKey(dataSet)) {
                return list_chart;
            }

            final Iterator<SwapsItemData> items_swapsDataset = list_mssql.get(dataSet).stream().
                    filter((data) -> (data.getTimestamp() > ServerTimeFrom)).
                    sorted(TickerItemComparator).
                    iterator();

            list_chart.add(new ArrayList<>());

            getSwapsDataInternal(items_swapsDataset, backtestHours, intervalMinutes, list_chart.get(list_chart.size() - 1));
        }
        return list_chart;
    }

    private void getSwapsDataInternal(Iterator<SwapsItemData> items_swapsDataset, final int backtestHours, int intervalMinutes, List<SwapsItemData> outputData) {
        // Timestamp
        final long cTime = (System.currentTimeMillis() / 1000l);
        final long startTime = cTime - (60l * 60l * backtestHours);
        long LastUsedTime = 0;

        float rate = 0, spot_price = 0;
        double amount_lent = 0;

        while (items_swapsDataset.hasNext()) {
            SwapsItemData item = items_swapsDataset.next();

            // Check if last added tick is above the threshold 'intervalMinutes'
            if (LastUsedTime + (intervalMinutes * 60) < item.getTimestamp()) {
                while (LastUsedTime + (intervalMinutes * 60) < item.getTimestamp()) {

                    if (item.getTimestamp() > startTime) {
                        rate = item.getRate();
                        spot_price = item.getSpotPrice();
                        amount_lent = item.getAmountLent();

                        // Add to list
                        outputData.add(
                                new SwapsItemData(
                                        rate,
                                        spot_price,
                                        amount_lent,
                                        (int) (LastUsedTime + (intervalMinutes * 60))));
                    }
                    // Reset
                    rate = 0;
                    spot_price = 0;
                    amount_lent = 0;

                    if (LastUsedTime == 0) {
                        LastUsedTime = item.getTimestamp();
                    }
                    LastUsedTime += (intervalMinutes * 60);
                }
            }
        }
        // For unmatured chart
        if (rate != 0 && spot_price != 0 && amount_lent != 0) {
            // Add to list
            outputData.add(
                    new SwapsItemData(
                            rate,
                            spot_price,
                            amount_lent,
                            (int) (LastUsedTime + (intervalMinutes * 60))));
        }
    }

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
            List<SwapsItemData> currentList = list_mssql.get(ExchangeCurrency);

            currentList.add(new SwapsItemData(rate, spot_price, amount_lent, timestamp));

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
                long biggest_server_time_result = MicrosoftAzureDatabaseExt.selectSwapsData(ExchangeSite, Currency, 60000, 24, LastCachedTime, list_newItems);
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
                            plew2.writeInt(data.getTimestamp());
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
