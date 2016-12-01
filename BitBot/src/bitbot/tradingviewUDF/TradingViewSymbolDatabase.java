package bitbot.tradingviewUDF;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zheng
 */
public class TradingViewSymbolDatabase {

    public static final List<TradingViewSymbol> SYMBOLS = new ArrayList();
    
    static {        
        // Mtgox
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "MtGox", "bitcoin"));
        
        // BTCe
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_EUR", "Bitcoin/ Euro", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_RUR", "Bitcoin/ Ruble", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_CNH", "Bitcoin/ Chinese Yuan", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_GBP", "Bitcoin/ Pound Sterling", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_USD", "Litecoin/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_BTC", "Litecoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_EUR", "Litecoin/ Euro", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_RUR", "Litecoin/ Ruble", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_CNH", "Litecoin/ Chinese Yuan", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_GBP", "Litecoin/ Pound Sterling", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("FTC_BTC", "Feathercoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XPM_BTC", "Primecoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("TRC_BTC", "Terracoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NMC_USD", "Namecoin/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NMC_BTC", "Namecoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NVC_USD", "Novacoin/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NVC_BTC", "Novacoin/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("PPC_USD", "Peercoin/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NVC_BTC", "Peercoin/ Bitcoin", "BTCe", "bitcoin"));
        
        SYMBOLS.add(new TradingViewSymbol("ETH_BTC", "Ethereum/ Bitcoin", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_USD", "Ethereum/ Dollar", "BTCe", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DSH_BTC", "Dash/ Bitcoin", "BTCe", "bitcoin"));
        
        // Coinpit
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Coinpit", "bitcoin"));
        
        // Bitfinex
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_USD", "Litecoin/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_BTC", "Litecoin/ Bitcoin", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DRK_USD", "Darkcoin/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DRK_BTC", "Darkcoin / Bitcoin", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_USD", "Ethereum/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_BTC", "Ethereum / Bitcoin", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETC_USD", "Ethereum Classic/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETC_BTC", "Ethereum Classic/ Bitcoin", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XMR_USD", "Monero/ Dollar", "Bitfinex", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XMR_BTC", "Monero/ Bitcoin", "Bitfinex", "bitcoin"));
        
        SYMBOLS.add(new TradingViewSymbol("SWAPS_TOTALBTC", "Total BTC swaps", "Bitfinex", "bitcoin", "bitfinex-btc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_TOTALLTC", "Total LTC swaps", "Bitfinex", "bitcoin", "bitfinex-ltc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_TOTALUSD", "Total USD swaps", "Bitfinex", "bitcoin", "bitfinex-usd"));
        //symbols.add(new TradingViewSymbol("SWAPS_TOTALDRK", "Total DRK swaps", "Bitfinex", "bitcoin", "bitfinex-drk"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_LONGSHORTRATIO", "Long vs Short Ratio", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_BULLBEARPERC", "Bull vs Bear percentage", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_BEARBULLPERC", "Bear vs Bull percentage", "Bitfinex", "bitcoin", "bitfinex-btc---bitfinex-usd---bitfinex-ltc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_BTCCOST", "BTC swap cost", "Bitfinex", "bitcoin", "bitfinex-btc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_LTCCOST", "LTC swap cost", "Bitfinex", "bitcoin", "bitfinex-ltc"));
        SYMBOLS.add(new TradingViewSymbol("SWAPS_USDCOST", "USD swap cost", "Bitfinex", "bitcoin", "bitfinex-usd"));
        //symbols.add(new TradingViewSymbol("SWAPS_DRKCOST", "DRK swap cost", "Bitfinex", "bitcoin", "bitfinex-drk"));
        
        // Bitstamp
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Bitstamp", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_EUR", "Bitcoin/ Euro", "Bitstamp", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("EUR_USD", "Euro/ Dollar", "Bitstamp", "bitcoin"));
        
        // Coinbase
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Coinbase", "bitcoin"));
        
        // Coinbase exchange , GDAX
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_GBP", "Bitcoin/ Pound Sterling", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_CAD", "Bitcoin/ Canadian Dollar", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_EUR", "Bitcoin/ Euro", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_BTC", "Ethereum / Bitcoin", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_USD", "Ethereum / Dollar", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_USD", "Litecoin / Dollar", "CoinbaseExchange", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_BTC", "Litecoin / Bitcoin", "CoinbaseExchange", "bitcoin"));
        
        // Itbit
        SYMBOLS.add(new TradingViewSymbol("XBT_USD", "Bitcoin/ Dollar", "Itbit", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XBT_SGD", "Bitcoin/ Singapore Dollar", "Itbit", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XBT_EUR", "Bitcoin/ Euro", "Itbit", "bitcoin"));
        
        // Okcoin
        SYMBOLS.add(new TradingViewSymbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Okcoin", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Okcoin", "bitcoin"));
        
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_USD", "Litecoin/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Weekly_USD", "Bitcoin Weekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures BiWeekly_USD", "Bitcoin Biweekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Quarterly_USD", "Bitcoin Quarterly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC Futures Weekly_USD", "Litecoin Weekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC Futures BiWeekly_USD", "Litecoin Biweekly Futures/ Dollar", "OkcoinInternational", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC Futures Quarterly_USD", "Litecoin Quarterly Futures/ Dollar", "OkcoinInternational", "bitcoin"));

        // Huobi
        SYMBOLS.add(new TradingViewSymbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "Huobi", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_CNY", "Litecoin/ Chinese Yuan", "Huobi", "bitcoin"));
        
        // BitVC
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Biweekly_CNY", "Bitcoin Biweekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Weekly_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Quarterly_CNY", "Bitcoin Quarterly Futures/ Chinese Yuan", "BitVC", "bitcoin"));
        
        // BTCChina
        SYMBOLS.add(new TradingViewSymbol("BTC_CNY", "Bitcoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_CNY", "Litecoin/ Chinese Yuan", "BTCChina", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_BTC", "Litecoin/ Bitcoin", "BTCChina", "bitcoin"));
        
        // CampBX
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Campbx", "bitcoin"));
        
        // Cryptsy
        SYMBOLS.add(new TradingViewSymbol("DOGE_BTC", "Dogecoin/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("CANN_BTC", "CannabisCoin/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DRK_BTC", "Darkcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("RDD_BTC", "Reddcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("URO_BTC", "Urocoins / Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BC_BTC", "Blackcoin/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTCD_BTC", "BitcoinDark/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DOGE_USD", "Dogecoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DRK_USD", "Darkcoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("FTC_USD", "Feathercoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_USD", "Litecoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("RDD_USD", "Reddcoin/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NXT_USD", "NXT/ Dollar", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("NXT_BTC", "NXT/ Bitcoin", "Cryptsy", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC_BTC", "Litecoin/ Bitcoin", "Cryptsy", "bitcoin"));
        
        // 796
        SYMBOLS.add(new TradingViewSymbol("BTC Futures_USD", "Bitcoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures_CNY", "Bitcoin Weekly Futures/ Chinese Yuan", "_796", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC Futures Quarterly_USD", "Bitcoin Quarterly Futures/ Dollar", "_796", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("LTC Futures_USD", "Litecoin Weekly Futures/ Dollar", "_796", "bitcoin"));
        
        // FybSG
        SYMBOLS.add(new TradingViewSymbol("BTC_SGD", "Bitcoin/ Singapore Dollar", "Fybsg", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("BTC_SEK", "Bitcoin/ Swedish Krona", "Fybse", "bitcoin"));
        
        // CEXIO
        SYMBOLS.add(new TradingViewSymbol("GHS_BTC", "Mining Gigahash/ Bitcoin", "CexIO", "bitcoin"));
        
        // DGEX
        SYMBOLS.add(new TradingViewSymbol("NXT_BTC", "NXT/ Bitcoin", "Dgex", "bitcoin"));
        
        // Kraken
        SYMBOLS.add(new TradingViewSymbol("XBT_USD", "Bitcoin/ Dollar", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("XBT_EUR", "Bitcoin/ Euro", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_XBT", "Ethereum/ Bitcoin", "Kraken", "bitcoin"));
        
        SYMBOLS.add(new TradingViewSymbol("ETC_USD", "Ethereum Classic/ Dollar", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETC_EUR", "Ethereum Classic/ Euro", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETC_ETH", "Ethereum Classic/ Ethereum", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETC_XBT", "Ethereum Classic/ Bitcoin", "Kraken", "bitcoin"));
        
        SYMBOLS.add(new TradingViewSymbol("DAO_XBT", "DAO/ Bitcoin", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_ETH", "DAO/ Ethereum", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_EUR", "DAO/ Euro", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_USD", "DAO/ Dollar", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_CAD", "DAO/ Canadian Dollar", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_GBP", "DAO/ Pound Sterling", "Kraken", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("DAO_JPY", "DAO/ Japanese Yen", "Kraken", "bitcoin"));
        
        // Gemini
        SYMBOLS.add(new TradingViewSymbol("BTC_USD", "Bitcoin/ Dollar", "Gemini", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_USD", "Ethereum/ Dollar", "Gemini", "bitcoin"));
        SYMBOLS.add(new TradingViewSymbol("ETH_BTC", "Ethereum/ Bitcoin", "Gemini", "bitcoin"));
        //kraken-xbt_usd---kraken-xbt_eur---
        // test
       //symbols.add(new TradingViewSymbol("^NSEI", "CNX NIFTY", "NSE", "index"));
        //symbols.add(new TradingViewSymbol("XOM", "Exxon Mobil Corporation", "NYSE", "stock"));
        
        List<TradingViewSymbol> vol_symbols = new ArrayList();
        
        for (TradingViewSymbol symbol : SYMBOLS) {
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
                    vol_symbols.add(new TradingViewSymbol(symbol.getName() + "VOL", symbol.getDescription(), symbol.getExchange(), symbol.getType()));
                    break;
            }
        }
        SYMBOLS.addAll(vol_symbols);
    }

    public static List<TradingViewSymbol> search(String searchText, String type, String exchange, int maxRecords) {
        int MAX_SEARCH_RESULTS = maxRecords <= 0 ? 50 : maxRecords;
        List<TradingViewSymbol> results = new ArrayList();
        
        final boolean queryIsEmpty = searchText == null || searchText.length() == 0;

        searchText = searchText.toLowerCase();
        
        for (TradingViewSymbol item : SYMBOLS) {
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

    public static TradingViewSymbol symbolInfo(String symbolName) {
        final String[] data = symbolName.split(":");
        final String exchange = (data.length > 1 ? data[0] : "").toUpperCase();
        final String symbol = (data.length > 1 ? data[1] : symbolName).toUpperCase();

        for (TradingViewSymbol item : SYMBOLS) {
            if (item.getName().toUpperCase().equals(symbol) &&  // Match ticker name
                    (exchange.length() == 0 || exchange.equals(item.getExchange().toUpperCase())) ) { // Match exchange name
                
                return item;
            }
        }
        return null;
    }
}
