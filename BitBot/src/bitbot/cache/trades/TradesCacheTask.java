package bitbot.cache.trades;

import bitbot.external.MicrosoftAzureDatabaseExt;
import bitbot.handler.channel.ChannelServer;
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
 * @author zheng
 */
public class TradesCacheTask {

    // MSSQL
    private static final int MSSQL_CacheRefreshTime_Seconds = 60;

    // Acquiring of old data from mssql database
    private final List<Integer> canAcceptNewInfoFromOtherPeers = new LinkedList(); // string = exchange+currency pair
    private final Map<String, List<TradesItemData>> list_mssql;
    private final List<LoggingSaveRunnable> runnable_mssql = new ArrayList();

    public TradesCacheTask() {
        this.list_mssql = new LinkedHashMap<>();

        StartScheduleTask();
    }

    public void StartScheduleTask() {
        for (String ExchangeCurrency : ChannelServer.getInstance().getCurrencyPairsForLargeTrades()) {
            final String[] Source_pair = ExchangeCurrency.split("-");
            final String ExchangeSite = Source_pair[0];
            final String Currency = Source_pair[1];

            // graph fetching from database
            if (ChannelServer.getInstance().isEnableTradesSQLDataAcquisition()) {
                final TradesCacheTask_MSSql tickercache = new TradesCacheTask_MSSql(ExchangeSite, Currency, ExchangeCurrency);
                final LoggingSaveRunnable runnable = TimerManager.register(tickercache, MSSQL_CacheRefreshTime_Seconds * 1000, Integer.MAX_VALUE);

                // set this reference so we are able to cancel this task later when we dont need the database anymore
                tickercache.setLoggingSaveRunnable(runnable);

                runnable_mssql.add(runnable);

                List<TradesItemData> arrays = new ArrayList(); // same reference
                list_mssql.put(ExchangeCurrency, arrays);
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
    }

    public List<TradesItemData> getTradesList(String ExchangeCurrency, float filterAbove, long timeAbove, long timeBelow) {
        List<TradesItemData> array = new ArrayList();

        if (list_mssql.containsKey(ExchangeCurrency)) {
            final List<TradesItemData> currentList = list_mssql.get(ExchangeCurrency);

            final Iterator<TradesItemData> streamMap = currentList.stream().filter((data) -> (
                            data.getLastPurchaseTime() > timeAbove && 
                            data.getLastPurchaseTime() < timeBelow && 
                            data.getAmount() >= filterAbove))
                    .sorted(TickerItemComparator)
                    .iterator();
            
            while (streamMap.hasNext()) {
                TradesItemData data = streamMap.next();
                
                array.add(data);
            }
        }
        return array;
    }

    private static final Comparator<Object> TickerItemComparator = (Object obj1, Object obj2) -> {
        TradesItemData data1 = (TradesItemData) obj1;
        TradesItemData data2 = (TradesItemData) obj2;

        if (data1.getLastPurchaseTime() > data2.getLastPurchaseTime()) {
            return 1;
        } else if (data1.getLastPurchaseTime() == data2.getLastPurchaseTime()) {
            return 0;
        }
        return -1;
    };

    public void receivedNewTradesEntry_OtherPeers(String ExchangeCurrency, float price, double amount, long LastPurchaseTime, byte type) {
        System.out.println(String.format("[Info] New trades info from other peers %s", ExchangeCurrency));

        if (list_mssql.containsKey(ExchangeCurrency)
                && canAcceptNewInfoFromOtherPeers.contains(ExchangeCurrency.hashCode())) { // First item, no sync needed
            List<TradesItemData> currentList = list_mssql.get(ExchangeCurrency);

            currentList.add(new TradesItemData(price, amount, LastPurchaseTime, TradeHistoryBuySellEnum.getEnumByValue(type)));

            //System.out.println("[Info] Added New info from other peers");
        }
    }

    private static final Object localStorageReadWriteMutex = new Object();

    public class TradesCacheTask_MSSql implements Runnable {

        private final String Currency;
        private final String ExchangeSite;
        private final String ExchangeCurrency;
        private long LastCachedTime = 0;

        // Switching between MSSQL and data from other peers
        private LoggingSaveRunnable runnable = null;
        private boolean isDataAcquisitionFromMSSQL_Completed = false; // just for reference, not using it for now
        private boolean IsFirstDataAcquisition = true; // Determines if this is the first time its acquiring data... [load from local storage]
        private boolean IsLoading = false;

        public TradesCacheTask_MSSql(String ExchangeSite, String Currency, String ExchangeCurrency) {
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

            System.out.println("[Info] Caching trades from SQLserv: " + ExchangeSite + ":" + Currency);

            final List<TradesItemData> currentList = list_mssql.get(ExchangeCurrency);
            final List<TradesItemData> list_newItems_storage = new ArrayList(); // create a new array first and replace later

            // Load data from local storage
            if (IsFirstDataAcquisition) {
                System.out.println("Caching trades data from storage: " + ExchangeCurrency);

                ReadFromFileStorage(list_newItems_storage);

                for (TradesItemData cur : list_newItems_storage) {
                    currentList.add(cur);
                }
                IsFirstDataAcquisition = false;
            }

            final List<TradesItemData> list_newItems = new ArrayList(); // create a new array first and replace later

            // Load data from sql server
            long biggest_server_time_result = MicrosoftAzureDatabaseExt.selectTradesData(ExchangeSite, Currency, 60000, LastCachedTime, list_newItems);
            if (biggest_server_time_result != -1) { // temporary network issue or unavailable
                // Set max server_time
                if (biggest_server_time_result > LastCachedTime) {
                    LastCachedTime = biggest_server_time_result;
                }
                for (TradesItemData cur : list_newItems) {
                    currentList.add(cur);
                }

                System.out.println("[Info] Caching trades data for " + ExchangeCurrency + " --> " + list_newItems.size() + ", MaxServerTime:" + LastCachedTime);

                if (list_newItems.size() <= 100) { // Are we done caching yet?
                    completedCaching();
                }
            }
            IsLoading = false;
        }

        private void completedCaching() {
            isDataAcquisitionFromMSSQL_Completed = true;
            canAcceptNewInfoFromOtherPeers.add(ExchangeCurrency.hashCode());
            runnable.getSchedule().cancel(false); // cancel this cache task completely.

            commitToFileStorage();

            System.out.println("[Info] Stopped caching " + ExchangeCurrency + " swap data from MSSQL");
        }

        private void ReadFromFileStorage(List<TradesItemData> list_newItems) {
            // Start saving to local file
            File f = new File("CachedTrades");
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
                                long timestamp = slea.readLong();
                                float price = slea.readFloat();
                                double amount = slea.readDouble();
                                TradeHistoryBuySellEnum type = TradeHistoryBuySellEnum.getEnumByValue(slea.readByte());

                                TradesItemData data = new TradesItemData(price, amount, timestamp, type);
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
            File f = new File("CachedTrades");
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
                    List<TradesItemData> currentList = new ArrayList(list_mssql.get(ExchangeCurrency));

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
                        for (TradesItemData data : currentList) {
                            PacketLittleEndianWriter plew2 = new PacketLittleEndianWriter(); // using multiple mplews to assist garbage collection
                            plew2.write(-1); // starting marker

                            plew2.writeLong(data.getLastPurchaseTime());
                            plew2.writeFloat(data.getPrice());
                            plew2.writeDouble(data.getAmount());
                            plew2.write(data.getType().getValue());

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
