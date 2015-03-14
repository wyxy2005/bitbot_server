package bitbot.handler.channel.tasks;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.server.Constants;
import bitbot.server.scripting.MarketBacktestBuySellPoints;
import bitbot.server.scripting.MarketBacktestInteraction;
import bitbot.server.scripting_support.MarketBacktestScriptManager;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

/**
 *
 * @author z
 */
public class BacktestTask implements Runnable {

    private final Response response;
    private final Request request;

    private String content;

    public BacktestTask(Request request, Response response, Query query) {
        this.response = response;
        this.request = request;

        try {
            content = request.getContent();
        } catch (IOException exp) {
            content = null;
        }
    }

    @Override
    public void run() {
        try {
            try (PrintStream body = response.getPrintStream()) {
                _ResponseHeader.addBasicResponseHeader(response);

                JSONObject returnJson = new JSONObject();

                if (content != null) {
                    MarketBacktestInteraction bt = new MarketBacktestInteraction("");

                    String returnResult = MarketBacktestScriptManager.startBacktestScriptfromString(bt, content);
                    if (returnResult == null) {
                        returnJson.put("success", "1");
                        returnJson.put("error_msg", "");

                        JSONArray array = new JSONArray();
                        JSONArray array_Debug = new JSONArray();

                        // back testing
                        for (MarketBacktestBuySellPoints pt : bt.getBuySellPoints()) {
                            JSONObject obj = new JSONObject();

                            obj.put("IsBuy", pt.isBuy() ? "buy" : "sell");
                            obj.put("Amount", pt.getAmount());
                            obj.put("Price", pt.getPrice());

                            TickerItem_CandleBar item = pt.getTickerItem_Candlebar();

                            obj.put("server_time", item.getServerTime());
                            obj.put("Open", item.getOpen());
                            obj.put("Close", item.getClose());
                            obj.put("High", item.getHigh());
                            obj.put("Low", item.getLow());
                            obj.put("Volume", item.getVol());
                            obj.put("VolumeCur", item.getVol_Cur());
                            obj.put("Ratio", item.getBuySell_Ratio());

                            array.add(obj);
                        }
                        returnJson.put("ret", array);

                        // Full candlestick data
                        JSONArray fullchartDataArray = new JSONArray();
                        if (bt.getLastTickerList_CandleStick() != null) {
                            bt.getLastTickerList_CandleStick().stream().map((item) -> {
                                JSONArray obj_array = new JSONArray();

                                obj_array.add(item.getServerTime());
                                obj_array.add(item.getOpen());
                                obj_array.add(item.getClose());
                                obj_array.add(item.getHigh());
                                obj_array.add(item.getLow());
                                obj_array.add(item.getVol());
                                obj_array.add(item.getVol_Cur());
                                obj_array.add(item.getBuySell_Ratio());

                                return obj_array;
                            }).forEach((obj) -> {
                                fullchartDataArray.add(obj);
                            });
                        }
                        returnJson.put("fullChartData", fullchartDataArray);

                        // back test msg
                        Collections.reverse(bt.getDebugMessages());
                        for (String str : bt.getDebugMessages()) {
                            JSONObject obj = new JSONObject();

                            obj.put("Str", str);

                            array_Debug.add(obj);
                        }
                        returnJson.put("Debug", array_Debug);

                        String retString = returnJson.toJSONString();

                        response.setContentLength(retString.length());
                        body.print(retString);
                    } else {
                        returnJson.put("success", "0");
                        returnJson.put("error_msg", returnResult);

                        String retString = returnJson.toJSONString();

                        response.setContentLength(retString.length());
                        body.print(retString);
                    }
                } else {
                    returnJson.put("success", "0");
                    returnJson.put("error_msg", "No content available.");

                    String retString = returnJson.toJSONString();

                    response.setContentLength(retString.length());
                    body.print(retString);
                }
                response.setStatus(Status.OK);
                body.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }
}
