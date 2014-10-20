package bitbot.server.scripting;

import bitbot.cache.tickers.TickerItem_CandleBar;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;

/**
 *
 * @author z
 */
public class MarketBacktestInteraction extends AbstractScriptInteraction {

    private double 
            InitialPrimaryCurrency = 0,
            InitialSecondaryCurrency = 10000;
    private final List<MarketBacktestBuySellPoints> BuySellPoints = new ArrayList<>();
    
    public MarketBacktestInteraction(String _startPath) {
        super(_startPath);
    }

    public void setInitialPrimaryCurrency(double value) {
        this.InitialPrimaryCurrency = value;
    }

    public void setInitialSecondaryCurrency(double value) {
        this.InitialSecondaryCurrency = value;
    }
    
    public double getInitialPrimaryCurrency() {
        return InitialPrimaryCurrency;
    }
    
    public double getInitialSecondaryCurrency() {
        return InitialSecondaryCurrency;
    }
    
    public List<MarketBacktestBuySellPoints> getBuySellPoints() {
        return BuySellPoints;
    }

    public void prepareForCompute(List<TickerItem_CandleBar> candlestickInput, double[]... value) {
        try {
            for (int i = 0; i < candlestickInput.size(); i++) {
                TickerItem_CandleBar candleStick = candlestickInput.get(i);

                List<Double> valueArrays = new ArrayList<>();
                for (double[] d : value) {
                    valueArrays.add(d[i]);
                }
                this.getInvocable().invokeFunction("compute", candleStick, valueArrays);
            }
        } catch (ScriptException | NoSuchMethodException exp) {
            throw new RuntimeException(exp.getMessage());
        }
    }

    @Override
    public void buyAmount(TickerItem_CandleBar tickerItem_Candlebar, float price, float amount) {
        double maxBuyFiatAmount = Math.min(InitialSecondaryCurrency, price * amount);
        if (maxBuyFiatAmount > 0) {
            InitialSecondaryCurrency -= maxBuyFiatAmount;
            InitialPrimaryCurrency += maxBuyFiatAmount / price;
            
            BuySellPoints.add(new MarketBacktestBuySellPoints(tickerItem_Candlebar, amount, price, true));
        }
    }

    @Override
    public void sellAmount(TickerItem_CandleBar tickerItem_Candlebar, float price, float amount) {
        double maxSellAmount = Math.min(InitialPrimaryCurrency, amount);
        if (maxSellAmount > 0) {
            InitialPrimaryCurrency -= maxSellAmount;
            InitialSecondaryCurrency += maxSellAmount * price;
            
            BuySellPoints.add(new MarketBacktestBuySellPoints(tickerItem_Candlebar, amount, price, false));
        }
    }

    @Override
    public void buyPercentage(TickerItem_CandleBar tickerItem_Candlebar, double price, float amount) {

    }

    @Override
    public void sellPercentage(TickerItem_CandleBar tickerItem_Candlebar, double price, float amount) {

    }
}
