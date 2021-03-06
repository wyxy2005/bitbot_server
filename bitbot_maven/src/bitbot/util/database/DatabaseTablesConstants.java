package bitbot.util.database;

/**
 *
 * @author z
 */
public class DatabaseTablesConstants {

    /**
     * Returns the database table name
     *
     * @param TmpExchangeSite {@code String} The exchange site
     * @param TmpcurrencyPair {@code String} The exchange pair
     *
     * @return String the output
     */
    public static String getDatabaseTableName(String TmpExchangeSite, String TmpcurrencyPair) {
        TmpExchangeSite = TmpExchangeSite.replace("_", "");

        char firstExchangeChar = TmpExchangeSite.charAt(0);
        if (firstExchangeChar >= '0' && firstExchangeChar <= '9') {
            TmpExchangeSite = String.format("a%s", TmpExchangeSite); // stupid mssql database requiring to start with a letter
        }
        TmpcurrencyPair = TmpcurrencyPair.replace(" ", "");

        final String tableName = String.format("%s_price_%s", TmpExchangeSite, TmpcurrencyPair);

        return tableName;
    }

    /**
     * Returns the database table name for trades
     * 
     * @param TmpExchangeSite
     * @param TmpcurrencyPair
     * @return
     */
    public static String getDatabaseTableName_Trades(String TmpExchangeSite, String TmpcurrencyPair) {
        TmpExchangeSite = TmpExchangeSite.replace("_", "");

        char firstExchangeChar = TmpExchangeSite.charAt(0);
        if (firstExchangeChar >= '0' && firstExchangeChar <= '9') {
            TmpExchangeSite = String.format("a%s", TmpExchangeSite); // stupid mssql database requiring to start with a letter
        }
        TmpcurrencyPair = TmpcurrencyPair.replace(" ", "");

        final String tableName = String.format("%s_trades_%s", TmpExchangeSite, TmpcurrencyPair);

        return tableName;
    }

    /**
     * Returns the database table name
     *
     * @param TmpExchangeSite {@code String} The exchange site
     * @param Tmpcurrency {@code String} The exchange pair
     *
     * @return String the output
     */
    public static String getSwapsDatabaseName(String TmpExchangeSite, String Tmpcurrency) {
        final String tableName = String.format("%s_swaps_%s", TmpExchangeSite, Tmpcurrency);

        return tableName;
    }
}
