package bitbot.cache.swaps;

import bitbot.cache.swaps.HTTP.Swaps_Bitfinex;
import bitbot.external.MicrosoftAzureExt;
import bitbot.handler.channel.ChannelServer;
import bitbot.server.ServerLog;
import bitbot.server.ServerLogType;
import bitbot.server.threads.LoggingSaveRunnable;
import bitbot.server.threads.TimerManager;
import java.util.ArrayList;
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
            }

            // History
            if (ChannelServer.getInstance().isEnableEnableSwaps()) {
                SwapsInterface swap = null;
                int DefaultUpdateTime = 5; // 5 minutes

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

    public class SwapsCacheTask_MSSql implements Runnable {

        private final String Currency;
        private final String ExchangeSite;
        private final String ExchangeCurrency;
        private long LastCachedTime = 0;

        // Switching between MSSQL and data from other peers
        private LoggingSaveRunnable runnable = null;
        private boolean isDataAcquisitionFromMSSQL_Completed = false; // just for reference, not using it for now

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
            if (isDataAcquisitionFromMSSQL_Completed) {
                return;
            }
            System.out.println("[Info] Caching swaps from SQLserv: " + ExchangeSite + ":" + Currency);

            List<SwapsItemData> list_newItems = new ArrayList(); // create a new array first and replace later
            long biggest_server_time_result = MicrosoftAzureExt.selectSwapsData(ExchangeSite, Currency, 999999, 24, LastCachedTime, list_newItems);
            if (biggest_server_time_result == -1) {
                return; // temporary network issue or unavailable
            }
            if (!list_newItems.isEmpty()) { // there's still something coming from the database, continue caching
                if (!list_mssql.containsKey(ExchangeCurrency)) { // First item, no sync needed
                    list_mssql.put(ExchangeCurrency, list_newItems);
                } else {
                    List<SwapsItemData> currentList = list_mssql.get(ExchangeCurrency);
                    list_newItems.stream().filter((data) -> (data.getTimestamp() > LastCachedTime)).map((data) -> {
                        currentList.add(data);
                        return data;
                    });
                }

                // Set max server_time
                if (biggest_server_time_result > LastCachedTime) {
                    LastCachedTime = biggest_server_time_result;
                }
            }
            System.out.println("[Info] Caching swap data for " + ExchangeCurrency + " --> " + list_newItems.size() + ", MaxServerTime:" + LastCachedTime);

            if (list_newItems.size() <= 1) { // Are we done caching yet?
                isDataAcquisitionFromMSSQL_Completed = true;
                canAcceptNewInfoFromOtherPeers.add(ExchangeCurrency.hashCode());
                runnable.getSchedule().cancel(false); // cancel this cache task completely.

                System.out.println("[Info] Stopped caching " + ExchangeCurrency + " swap data from MSSQL");
            }
        }

        private void commitToFileStorage() {
        }
    }
   
}
