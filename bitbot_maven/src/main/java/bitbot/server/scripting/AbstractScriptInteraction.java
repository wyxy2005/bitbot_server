package bitbot.server.scripting;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.script.Invocable;

/**
 *
 * @author z
 */
public class AbstractScriptInteraction {

    private final String startPath;
    private final Map<String, MutableClass> context = new HashMap();
    private Invocable iv;
    private final List<String> DebugMessages = new ArrayList<>();
    
    private List<TickerItem_CandleBar> ret_candlestick_cached = null;
    
    public AbstractScriptInteraction(String _startPath) {
        this.startPath = _startPath;
    }
    
    public void setInvocable(Invocable iv){
        this.iv = iv;
    }
    
    protected Invocable getInvocable() {
        return iv;
    }

    public String getScriptPath() {
        return startPath;
    }
    
    public List<String> getDebugMessages() {
        return DebugMessages;
    }
    
    public double getHighest(double[] val) {
        double highest = Double.MIN_VALUE;
        for (double d : val) {
            if (d > highest) {
                highest = d;
            }
        }
        return highest;
    }
    
    public double getLowest(double[] val) {
        double lowest = Double.MAX_VALUE;
        for (double d : val) {
            if (d < lowest) {
                lowest = d;
            }
        }
        return lowest;
    }

    public double[] createDoubleArray(int size) {
        return new double[size];
    }

    public String[] createStringArray(int size) {
        return new String[size];
    }

    public int[] createIntArray(int size) {
        return new int[size];
    }

    public byte[] createByteArray(int size) {
        return new byte[size];
    }

    public short[] createShortArray(int size) {
        return new short[size];
    }
    
    public Object getContext(String key) {
        if (context.containsKey(key)) {
            return context.get(key).get();
        }
        return null;
    }

    public void setContext(String key, String value) {
        MutableClass value_ = new MutableClass();
        value_.set(value);

        if (context.containsKey(key)) {
            context.replace(key, value_);
        } else {
            context.put(key, value_);
        }
    }


    public List<TickerItem_CandleBar> getTickerList_Candlestick(final String ticker, final int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom) {
        List<TickerItem_CandleBar> ret = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(ticker, backtestHours, intervalMinutes, ExchangeSite, ServerTimeFrom, System.currentTimeMillis() / 1000, true, false);
        if (ret.isEmpty()) {
            throw new RuntimeException("Not enough data available.");
        }
        ret_candlestick_cached = ret;
        return ret;
    }
    
    public List<TickerItem_CandleBar> getLastTickerList_CandleStick() {
        return ret_candlestick_cached;
    }
    
    public double[] getClosingPriceDoubleArray(List<TickerItem_CandleBar> result) {
        double[] returnRet = new double[result.size()];

        int i = 0;
        Iterator<TickerItem_CandleBar> ltr = result.iterator();
        while (ltr.hasNext()) {
            returnRet[i] = ltr.next().getClose();
            i++;
        }
        //System.out.println(Arrays.toString(returnRet));
        return returnRet;
    }

    public MInteger createMInteger() {
        MInteger integer = new MInteger();

        return integer;
    }

    public Core getTaLibCore() {
        Core c = new Core();

        return c;
    }
    
    public void SentEmail(String email, String Title, String contnet) {
        // TODO: SendGrid
    }
    
    public void buyAmount(TickerItem_CandleBar tickerItem_Candlebar, float price, float amount) {
        
    }
    
    public void sellAmount(TickerItem_CandleBar tickerItem_Candlebar, float price, float amount) {
        
    }
    
    public void buyPercentage(TickerItem_CandleBar tickerItem_Candlebar, double price, float amount) {
        
    }
    
    public void sellPercentage(TickerItem_CandleBar tickerItem_Candlebar, double price, float amount) {
        
    }
    
    public void appendDebugMessage(String message) {
        DebugMessages.add(message);
        System.out.println(message);
    }
}
