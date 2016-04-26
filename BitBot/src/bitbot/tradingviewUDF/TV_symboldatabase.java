package bitbot.tradingviewUDF;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zheng
 */
public class TV_SymbolDatabase {

    public static final List<TV_Symbol> symbols = new ArrayList();
    
    static {        
        // Mtgox
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "MtGox", "bitcoin"));
        
        // BTCe
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
        
        // Bitfinex
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_USD", "Litecoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_USD", "Darkcoin/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("DRK_BTC", "Darkcoin / Bitcoin", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("ETH_USD", "Ethereum/ Dollar", "Bitfinex", "bitcoin"));
        symbols.add(new TV_Symbol("ETH_BTC", "Ethereum / Bitcoin", "Bitfinex", "bitcoin"));
        
        symbols.add(new TV_Symbol("SWAPS_TOTALBTC", "Total BTC swaps", "Bitfinex", "bitcoin", "bitfinex-btc"));
        symbols.add(new TV_Symbol("SWAPS_TOTALLTC", "Total LTC swaps", "Bitfinex", "bitcoin", "bitfinex-ltc"));
        symbols.add(new TV_Symbol("SWAPS_TOTALUSD", "Total USD swaps", "Bitfinex", "bitcoin", "bitfinex-usd"));
        //symbols.add(new TV_Symbol("SWAPS_TOTALDRK", "Total DRK swaps", "Bitfinex", "bitcoin", "bitfinex-drk"));
        symbols.add(new TV_Symbol("SWAPS_LONGSHORTRATIO", "Long vs Short Ratio", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        symbols.add(new TV_Symbol("SWAPS_BULLBEARPERC", "Bull vs Bear percentage", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        symbols.add(new TV_Symbol("SWAPS_BEARBULLPERC", "Bear vs Bull percentage", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        symbols.add(new TV_Symbol("SWAPS_BTCCOST", "BTC swap cost", "Bitfinex", "bitcoin", "bitfinex-btc"));
        symbols.add(new TV_Symbol("SWAPS_LTCCOST", "LTC swap cost", "Bitfinex", "bitcoin", "bitfinex-ltc"));
        symbols.add(new TV_Symbol("SWAPS_USDCOST", "USD swap cost", "Bitfinex", "bitcoin", "bitfinex-usd"));
        //symbols.add(new TV_Symbol("SWAPS_DRKCOST", "DRK swap cost", "Bitfinex", "bitcoin", "bitfinex-drk"));
        
        // Bitstamp
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Bitstamp", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_EUR", "Bitcoin/ Euro", "Bitstamp", "bitcoin"));
        
        // Coinbase
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Coinbase", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "CoinbaseExchange", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_GBP", "Bitcoin/ Pound Sterling", "CoinbaseExchange", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_CAD", "Bitcoin/ Canadian Dollar", "CoinbaseExchange", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_EUR", "Bitcoin/ Euro", "CoinbaseExchange", "bitcoin"));
        
        // Itbit
        symbols.add(new TV_Symbol("XBT_USD", "Bitcoin/ Dollar", "Itbit", "bitcoin"));
        symbols.add(new TV_Symbol("XBT_SGD", "Bitcoin/ Singapore Dollar", "Itbit", "bitcoin"));
        symbols.add(new TV_Symbol("XBT_EUR", "Bitcoin/ Euro", "Itbit", "bitcoin"));
        
        // Okcoin
        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Okcoin", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Okcoin", "bitcoin"));
        
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_USD", "Litecoin/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Weekly_USD", "Bitcoin Weekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures BiWeekly_USD", "Bitcoin Biweekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Quarterly_USD", "Bitcoin Quarterly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("LTC Futures Weekly_USD", "Litecoin Weekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("LTC Futures BiWeekly_USD", "Litecoin Biweekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        symbols.add(new TV_Symbol("LTC Futures Quarterly_USD", "Litecoin Quarterly Futures/ Dollar", "OkcoinInternational", "bitcoin"));

        // Huobi
        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Huobi", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Huobi", "bitcoin"));
        
        // BitVC
        symbols.add(new TV_Symbol("BTC Futures Biweekly_CNY", "Bitcoin Biweekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Weekly_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Quarterly_CNY", "Bitcoin Quarterly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        
        // BTCChina
        symbols.add(new TV_Symbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_CNY", "Litecoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "BTCChina", "bitcoin"));
        
        // CampBX
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Campbx", "bitcoin"));
        
        // Cryptsy
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
        symbols.add(new TV_Symbol("NXT_BTC", "NXT/ Bitcoin", "Cryptsy", "bitcoin"));
        symbols.add(new TV_Symbol("LTC_BTC", "Litecoin/ Bitcoin", "Cryptsy", "bitcoin"));
        
        // 796
        symbols.add(new TV_Symbol("BTC Futures_USD", "Bitcoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("BTC Futures Quarterly_USD", "Bitcoin Quarterly Futures/ Dollar", "_796", "bitcoin"));
        symbols.add(new TV_Symbol("LTC Futures_USD", "Litecoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        
        // FybSG
        symbols.add(new TV_Symbol("BTC_SGD", "Bitcoin/ Singapore Dollar", "Fybsg", "bitcoin"));
        symbols.add(new TV_Symbol("BTC_SEK", "Bitcoin/ Swedish Krona", "Fybse", "bitcoin"));
        
        // CEXIO
        symbols.add(new TV_Symbol("GHS_BTC", "Mining Gigahash/ Bitcoin", "CexIO", "bitcoin"));
        
        // DGEX
        symbols.add(new TV_Symbol("NXT_BTC", "NXT/ Bitcoin", "Dgex", "bitcoin"));
        
        // Kraken
        symbols.add(new TV_Symbol("XBT_USD", "Bitcoin/ Dollar", "Kraken", "bitcoin"));
        symbols.add(new TV_Symbol("XBT_EUR", "Bitcoin/ Euro", "Kraken", "bitcoin"));
        symbols.add(new TV_Symbol("ETH_XBT", "Ethereum/ Bitcoin", "Kraken", "bitcoin"));
        
        // Gemini
        symbols.add(new TV_Symbol("BTC_USD", "Bitcoin/ Dollar", "Gemini", "bitcoin"));
        //kraken-xbt_usd---kraken-xbt_eur---
        // test
       //symbols.add(new TV_Symbol("^NSEI", "CNX NIFTY", "NSE", "index"));
        //symbols.add(new TV_Symbol("XOM", "Exxon Mobil Corporation", "NYSE", "stock"));
        
        List<TV_Symbol> vol_symbols = new ArrayList();
        
        for (TV_Symbol symbol : symbols) {
            switch (symbol.getExchange()) {
                case "BTCe":
                case "Bitfinex":
                case "CoinbaseExchange":
                case "Okcoin":
                case "OkcoinInternational":
                case "Huobi":
                case "BitVC":
                case "BTCChina":
                case "Cryptsy":
                case "_796":
                case "Gemini":
                    vol_symbols.add(new TV_Symbol(symbol.getName() + "VOL", symbol.getDescription(), symbol.getExchange(), symbol.getType()));
                    break;
            }
        }
        symbols.addAll(vol_symbols);
    }

    public static List<TV_Symbol> search(String searchText, String type, String exchange, int maxRecords) {
        int MAX_SEARCH_RESULTS = maxRecords <= 0 ? 50 : maxRecords;
        List<TV_Symbol> results = new ArrayList();
        
        final boolean queryIsEmpty = searchText == null || searchText.length() == 0;

        searchText = searchText.toLowerCase();
        
        for (TV_Symbol item : symbols) {
            if (type.length() > 0 && !item.getType().equalsIgnoreCase(type)) {
                continue;
            }
            if (exchange.length() > 0 && !item.getExchange().equalsIgnoreCase(exchange)) {
                continue;
            }
            //System.out.println(item.name + " " + item.description + " " + item.type + " " + item.exchange);
            if (queryIsEmpty 
                    || (item.getName().toLowerCase().contains(searchText)) 
                    || (item.getExchange() + ":" + item.getName()).toLowerCase().contains(searchText)
                    ) {
                
                results.add(item);

            }
            if (results.size() >= MAX_SEARCH_RESULTS) {
                break;
            }
        }
        return results;
    }

    public static TV_Symbol symbolInfo(String symbolName) {
        final String[] data = symbolName.split(":");
        final String exchange = (data.length > 1 ? data[0] : "").toUpperCase();
        final String symbol = (data.length > 1 ? data[1] : symbolName).toUpperCase();

        for (TV_Symbol item : symbols) {
            if (item.getName().toUpperCase().equals(symbol) &&  // Match ticker name
                    (exchange.length() == 0 || exchange.equals(item.getExchange().toUpperCase())) ) { // Match exchange name
                
                return item;
            }
        }
        return null;
    }
}
