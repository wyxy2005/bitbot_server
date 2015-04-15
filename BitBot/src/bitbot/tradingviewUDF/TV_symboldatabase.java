package bitbot.tradingviewUDF;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zheng
 */
public class TV_symboldatabase {

    public static final List<TV_Symbol> symbols = new ArrayList();

    static {
        // Name description exchange type
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_EUR", "Bitcoin/ Euro", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_RUR", "Bitcoin/ Ruble", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_CNH", "Bitcoin/ Chinese Yuan", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_GBP", "Bitcoin/ Pound Sterling", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_USD", "Litecoin/ Dollar", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_EUR", "Litecoin/ Euro", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_RUR", "Litecoin/ Ruble", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNH", "Litecoin/ Chinese Yuan", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_GBP", "Litecoin/ Pound Sterling", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("FTC_BTC", "Feathercoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("XPM_BTC", "Primecoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("TRC_BTC", "Terracoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("NMC_USD", "Namecoin/ Dollar", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("NMC_BTC", "Namecoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("NVC_USD", "Novacoin/ Dollar", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("NVC_BTC", "Novacoin/ Bitcoin", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("PPC_USD", "Peercoin/ Dollar", "BTCe", "bitcoin"));
        symbols.add(new TV_Symbol("NVC_BTC", "Peercoin/ Bitcoin", "BTCe", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_USD", "Litecoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_USD", "Darkcoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_BTC", "Darkcoin / Bitcoin", "Bitfinex", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Bitstamp", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Coinbase", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "CoinbaseExchange", "bitcoin"));
        
        symbols.add(new TV_Symbol("XBT_USD", "Bitcoin/ Dollar", "Itbit", "bitcoin"));
        symbols.add(new TV_Symbol("XBT_SGD", "Bitcoin/ Singapore Dollar", "Itbit", "bitcoin"));
        symbols.add(new TV_Symbol("XBT_EUR", "Bitcoin/ Euro", "Itbit", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Okcoin", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Okcoin", "bitcoin"));

        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Huobi", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Huobi", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC Futures Quarterly_CNY", "Bitcoin Quarterly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures BIWEEKLY_CNY", "Bitcoin Biweekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures WEEKLY_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "BTCChina", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Campbx", "bitcoin"));
        
        symbols.add(new TV_Symbol("DOGE_BTC", "Dogecoin/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("CANN_BTC", "CannabisCoin/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_BTC", "Darkcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("RDD_BTC", "Reddcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("URO_BTC", "Urocoins / Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("BC_BTC", "Blackcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("BTCD_BTC", "BitcoinDark/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("DOGE_USD", "Dogecoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_USD", "Darkcoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("FTC_USD", "Feathercoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_USD", "Litecoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("RDD_USD", "Reddcoin/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("NXT_USD", "NXT/ Dollar", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "Cryptsy", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC Futures_USD", "Bitcoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Quarterly_USD", "Bitcoin Quarterly Futures/ Dollar", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("LTC Futures_USD", "Litecoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_SGD", "Bitcoin/ Singapore Dollar", "Fybse", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_SEK", "Bitcoin/ Swedish Krona", "Fybsg", "bitcoin"));
        
        symbols.add(new TV_Symbol("GHS_BTC", "Mining Gigahash/ Bitcoin", "CexIO", "bitcoin"));
        
        symbols.add(new TV_Symbol("NXT_BTC", "NXT/ Bitcoin", "Dgex", "bitcoin"));
        
        //kraken-xbt_usd---kraken-xbt_eur---
        // test
       //symbols.add(new TV_Symbol("^NSEI", "CNX NIFTY", "NSE", "index"));
        //symbols.add(new TV_Symbol("XOM", "Exxon Mobil Corporation", "NYSE", "stock"));
    }

    public static List<TV_Symbol> search(String searchText, String type, String exchange, int maxRecords) {
        int MAX_SEARCH_RESULTS = maxRecords <= 0 ? 50 : maxRecords;
        List<TV_Symbol> results = new ArrayList();
        
        final boolean queryIsEmpty = searchText == null || searchText.length() == 0;

        searchText = searchText.toLowerCase();
        
        for (TV_Symbol item : symbols) {
            if (type.length() > 0 && !item.type.equalsIgnoreCase(type)) {
                continue;
            }
            if (exchange.length() > 0 && !item.exchange.equalsIgnoreCase(exchange)) {
                continue;
            }
            //System.out.println(item.name + " " + item.description + " " + item.type + " " + item.exchange);
            if (queryIsEmpty || item.name.toLowerCase().contains(searchText) 
                || (item.exchange + ":" + item.name).toLowerCase().contains(searchText)) {
                results.add(item.cloneObject());
            }
            if (results.size() >= MAX_SEARCH_RESULTS) {
                break;
            }
        }
        return results;
    }

    public static TV_Symbol symbolInfo(String symbolName) {
        String[] data = symbolName.split(":");
        String exchange = (data.length > 1 ? data[0] : "").toUpperCase();
        String symbol = (data.length > 1 ? data[1] : symbolName).toUpperCase();

        for (TV_Symbol item : symbols) {
            if (item.name.toUpperCase().equals(symbol) && (exchange.length() == 0 || exchange.equals(item.exchange.toUpperCase()))) {
                return item;
            }
        }
        return null;
    }
}
