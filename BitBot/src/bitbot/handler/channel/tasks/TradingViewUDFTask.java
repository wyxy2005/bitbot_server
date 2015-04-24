package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItemData;
import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import bitbot.Constants;
import bitbot.tradingviewUDF.TV_Symbol;
import bitbot.tradingviewUDF.TV_symboldatabase;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 *
 * @author zheng
 */
public class TradingViewUDFTask implements Runnable {

    private final Response response;
    private final Request request;
    private final Query query;
    private final String path;

    public TradingViewUDFTask(Request request, Response response, Query query, String path) {
        this.response = response;
        this.request = request;
        this.query = query;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            PrintStream body = response.getPrintStream();

            _ResponseHeader.addBasicResponseHeader(response);
//System.out.println("Geting path: " + path);

            try {
                switch (path) {
                    case "/config":
                        this.sendConfig(body);
                        break;
                    case "/symbols":
                        this.sendSymbolInfo(body,
                                query.get("symbol"));
                        break;
                    case "/search":
                        this.sendSymbolSearchResults(body,
                                query.get("query"), query.get("type"), query.get("exchange"), Integer.parseInt(query.get("limit")));
                        break;
                    case "/history":
                        this.sendSymbolHistory(body,
                                query.get("symbol"), Long.parseLong(query.get("from")), Long.parseLong(query.get("to")), query.get("resolution"));
                        break;
                    case "/quotes":
                        break;
                    case "/marks":
                        break;
                    case "/symbol_info": {
                        /*     final String group = query.get("group"); // NYSE AMEX

                         for (Entry<String, String> x : query.entrySet()) {
                         System.out.println(x.getKey() + " - " + x.getValue());
                         }
                         sendError(body, "wrong_request_format");*/
                        break;
                    }
                    default:
                        sendError(body, "wrong_request_format");
                        break;
                }
            } catch (Exception exp) {
                exp.printStackTrace();
                sendError(body, "wrong_request_format");
            } finally {
                body.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendError(PrintStream body, String error) {
        String ret = "{\"s\":\"error\",\"errmsg\":\"" + error + "\"}";

        response.setContentLength(ret.length());
        body.print(ret);
    }

    private void sendSymbolSearchResults(PrintStream body, String query, String type, String exchange, int maxRecords) {
        List<TV_Symbol> searchResult = TV_symboldatabase.search(query, type, exchange, maxRecords);

        JSONArray json_array = new JSONArray();
        for (TV_Symbol sym : searchResult) {
            JSONObject obj = new JSONObject();

            obj.put("symbol", sym.exchange + ":" + sym.name);
            obj.put("full_name", sym.exchange + ":" + sym.name);
            obj.put("description", sym.description);
            obj.put("exchange", sym.exchange);
            obj.put("type", sym.type);

            json_array.add(obj);
        }
        String ret = json_array.toJSONString();

        response.setContentLength(ret.length());
        body.print(ret);
    }

    private void sendSymbolHistory(PrintStream body, String symbolName, long startDateTimestamp, long endDateTimestamp, String resolution) {
        // Get symbol from database
        TV_Symbol symbol = TV_symboldatabase.symbolInfo(symbolName);
        if (symbol == null) {
            sendError(body, "unknown_symbol");
            return;
        } else if (startDateTimestamp <= 0 || endDateTimestamp <= 0) {
            sendError(body, "invalid entry");
            return;
        }
        //System.out.println("[History] Symbol: " + symbolName + ", date: " + startDateTimestamp + ", res: " + resolution);

        // Parse resolution from string
        int time = 60; // 1 hour for default.
        if (resolution.endsWith("m")) {
            if (resolution.length() == 1) {
                time = 1;
            } else {
                time = 1 * Integer.parseInt(resolution.substring(0, resolution.length() - 1));
            }
        } else if (resolution.endsWith("H")) {
            if (resolution.length() == 1) {
                time = 60;
            } else {
                time = 60 * Integer.parseInt(resolution.substring(0, resolution.length() - 1));
            }
        } else if (resolution.endsWith("D")) {
            if (resolution.length() == 1) {
                time = 1440;
            } else {
                time = 1440 * Integer.parseInt(resolution.substring(0, resolution.length() - 1));
            }
        } else if (resolution.endsWith("W")) {
            if (resolution.length() == 1) {
                time = 10080;
            } else {
                time = 10080 * Integer.parseInt(resolution.substring(0, resolution.length() - 1));
            }
        } else if (resolution.endsWith("M")) {
            if (resolution.length() == 1) {
                time = 40320;
            } else {
                time = 40320 * Integer.parseInt(resolution.substring(0, resolution.length() - 1));
            }
        } else {
            char lastChar = resolution.charAt(resolution.length() - 1);
            if (lastChar >= '0' && lastChar <= '9') {
                time = Integer.parseInt(resolution);
            } else {
                sendError(body, "Unsupported resolution: " + resolution);
                return;
            }
        }
        
        List<TickerItem_CandleBar> ret = null;
        
        // Estimate the amount of candles needed
        //long datesRangeRight = Math.round(System.currentTimeMillis() / 1000); 
        //long datesRangeLeft = datesRangeRight - periodLengthSeconds(resolution); //	BEWARE: please note we really need 2 bars, not the only last one see the explanation below. `10` is the `large enough` value to work around holidays

        final long timeDifference = endDateTimestamp - startDateTimestamp;
        final int candlesRequested = (int) (timeDifference / (60 * time));
        
        //System.out.println(timeDifference + " server diff: " + (datesRangeLeft - datesRangeRight));
        
        int limit = 0;
        if (time <= 0) {
            sendError(body, "invalid entry");
            return;
        } else if (time <= 3) {
            limit = 6000;
        } else if (time <= 15) {
            limit = 3500;
        } else if (time <= 240) {
            limit = 3200; 
        } else {
            limit = 3000;
        }
 
        if (candlesRequested < limit) { // TV usually request 2041 at once... 
            ret = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(
                    symbol.name.toLowerCase(), 0, time, symbol.exchange.toLowerCase(), startDateTimestamp, Math.min(System.currentTimeMillis() / 1000, endDateTimestamp), true);
        } else {
            // should we auto ban?
            
        }
        // return result to client
        JSONObject json_main = new JSONObject();

        if (ret == null || ret.isEmpty()) {
            json_main.put("s", "no_data"); // Status code. Expected values: “ok” | “error” | “incomplete” | “no_data”
            json_main.put("errmsg", "Not enough data, please wait."); // Status code. Expected values: “ok” | “error” | “incomplete” | “no_data”
        } else {
            json_main.put("s", "ok");
        }

        JSONArray json_array_candlestick_t = new JSONArray();
        JSONArray json_array_candlestick_c = new JSONArray();
        JSONArray json_array_candlestick_o = new JSONArray();
        JSONArray json_array_candlestick_h = new JSONArray();
        JSONArray json_array_candlestick_l = new JSONArray();
        JSONArray json_array_candlestick_v = new JSONArray();

        for (TickerItem_CandleBar candle : ret) {
            json_array_candlestick_t.add(candle.getServerTime());
            json_array_candlestick_c.add(candle.getClose());
            json_array_candlestick_o.add(candle.getOpen());
            json_array_candlestick_h.add(candle.getHigh());
            json_array_candlestick_l.add(candle.getLow());
            json_array_candlestick_v.add(candle.getVol_Cur());
        }
        json_main.put("t", json_array_candlestick_t);
        json_main.put("c", json_array_candlestick_c);
        json_main.put("o", json_array_candlestick_o);
        json_main.put("h", json_array_candlestick_h);
        json_main.put("l", json_array_candlestick_l);
        json_main.put("v", json_array_candlestick_v);

        String retstr = json_main.toJSONString();

        response.setContentLength(retstr.length());
        body.print(retstr);
    }

   /* private int periodLengthSeconds(String resolution) {
	int daysCount = 0;
        final int requiredPeriodsCount = 10;

	if (resolution.endsWith("D")) {
            daysCount = requiredPeriodsCount;
	}
	else if (resolution.endsWith("M")) {
            daysCount = 31 * requiredPeriodsCount;
	}
	else if (resolution.endsWith("W")) {
            daysCount = 7 * requiredPeriodsCount;
	}
	else {
            daysCount = requiredPeriodsCount * Integer.parseInt(resolution) / (24 * 60);
	}
	return daysCount * 24 * 60 * 60;
}*/
    
    private void sendSymbolInfo(PrintStream body, String symbolName) {
        TV_Symbol symbol = TV_symboldatabase.symbolInfo(symbolName);
        if (symbol == null) {
            sendError(body, "unknown_symbol");
            return;
        }
        final TickerItemData summary_ret = ChannelServer.getInstance().getTickerTask().getTickerSummary(symbol.name.toLowerCase(), symbol.exchange.toLowerCase());

//	BEWARE: this `pricescale` parameter computation algorithm is wrong and works
//	for symbols with 10-based minimal movement value only
        String strPrice = summary_ret == null ? "1" : String.valueOf(summary_ret.getOpen());
        int integerPlaces = strPrice.indexOf('.');
        int decimalPlaces = strPrice.length() - integerPlaces - 1;

        JSONObject json_main = new JSONObject();

        json_main.put("name", symbol.name);
        json_main.put("exchange-traded", true);
        json_main.put("exchange-listed", true);
        json_main.put("minmov", 1);
        json_main.put("minmov2", 0);
        json_main.put("pricescale", Math.pow(10, decimalPlaces));
        json_main.put("pointvalue", 1);
        json_main.put("timezone", "UTC");
        json_main.put("session", "24x7"); // 0000-0000
        json_main.put("has_intraday", true);
        json_main.put("has_daily", true);
        //json_main.put("has_fractional_volume", true); // obselete
        json_main.put("has_weekly_and_monthly", true);
        json_main.put("has_empty_bars", false);
        json_main.put("has_no_volume", false);
        json_main.put("listed_exchange", symbol.exchange); // listed and traded exchange for bitcoin is the same
        json_main.put("exchange", symbol.exchange); // listed and traded exchange for bitcoin is the same
        json_main.put("ticker", symbol.exchange + ":" + symbol.name);
        json_main.put("description", symbol.description);
        json_main.put("type", symbol.type);

        // Contract expiration
        boolean expiredContract = false;
        if (expiredContract) {
            json_main.put("expired", false); // Boolean showing whether the symbol is an expired futures contract of not.
            json_main.put("expiration_date", System.currentTimeMillis() / 1000); // test 
        }

        // Supported resolutions
        JSONArray json_array_supportedres = new JSONArray();
        for (String res : Constants.tv_supportedResolutions) {
            json_array_supportedres.add(res);
        }
        json_main.put("supported_resolutions", json_array_supportedres);

        String retstr = json_main.toJSONString();

        response.setContentLength(retstr.length());
        body.print(retstr);
    }

    private void sendConfig(PrintStream body) {
        JSONObject json_main = new JSONObject();

        // main
        json_main.put("supports_search", Constants.tv_supports_search);
        json_main.put("supports_group_request", Constants.tv_supports_group_request);
        json_main.put("supports_marks", Constants.tv_supports_marks);

        // exchanges
        JSONArray json_array_exchanges = new JSONArray();
        for (int i = 0; i < Constants.tv_exchange_value.length; i++) {
            JSONObject json_sym = new JSONObject();

            json_sym.put("value", Constants.tv_exchange_value[i]);
            json_sym.put("name", Constants.tv_exchange_name[i]);
            json_sym.put("desc", Constants.tv_exchange_desc[i]);

            json_array_exchanges.add(json_sym);
        }
        json_main.put("exchanges", json_array_exchanges);

        // symbol types
        JSONArray json_array_symbolTypes = new JSONArray();
        for (int i = 0; i < Constants.tv_symbolType1.length; i++) {
            JSONObject json_sym = new JSONObject();

            json_sym.put("name", Constants.tv_symbolType1[i]);
            json_sym.put("value", Constants.tv_symbolType2[i]);

            json_array_symbolTypes.add(json_sym);
        }
        json_main.put("symbolsTypes", json_array_symbolTypes);

        // Supported resolutions
        JSONArray json_array_supportedres = new JSONArray();
        for (String res : Constants.tv_supportedResolutions) {
            json_array_supportedres.add(res);
        }
        json_main.put("supportedResolutions", json_array_supportedres);

        String retstr = json_main.toJSONString();

        response.setContentLength(retstr.length());
        body.print(retstr);
    }
}
