package bitbot.server.scripting;

import bitbot.cache.tickers.TickerItem_CandleBar;
import bitbot.handler.channel.ChannelServer;
import com.tictactec.ta.lib.Core;
import com.tictactec.ta.lib.MInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author z
 */
public class AbstractScriptInteraction {

    private final String startPath;
    private final Map<String, MutableClass> context = new HashMap();

    public AbstractScriptInteraction(String _startPath) {
        this.startPath = _startPath;
    }

    public String getScriptPath() {
        return startPath;
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

    public double[] getClosingPriceArray(final String ticker, final int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom) {
        List<TickerItem_CandleBar> result = ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(ticker, backtestHours, intervalMinutes, ExchangeSite, ServerTimeFrom);

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

    public List<TickerItem_CandleBar> getTickerList_Candlestick(final String ticker, final int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom) {
        return ChannelServer.getInstance().getTickerTask().getTickerList_Candlestick(ticker, backtestHours, intervalMinutes, ExchangeSite, ServerTimeFrom);
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
}
