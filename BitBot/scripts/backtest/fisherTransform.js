/*
 * TaLib documentation: http://www.heatonresearch.com/content/technical-analysis-library-ta-lib-tutorial-java
 * Java <> JS scripting guide: http://docs.oracle.com/javase/7/docs/technotes/guides/scripting/programmer_guide/
*/

var period_minute = 10; // 10 minutes

var prev_value = 0;
var prev_fisher = 0;

function initialize() {
    bt.setInitialPrimaryCurrency(0); // BTC
    bt.setInitialSecondaryCurrency(10000); // USD

    var talib = bt.getTaLibCore();
    var list_candlestick = bt.getTickerList_Candlestick(
        "btc_cny",
        (Math.max(5, (period_minute) * 20) / 5),
        period_minute,
        "okcoin",
        0); // String ticker, int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom
    var closingArray = bt.getClosingPriceDoubleArray(list_candlestick);



    bt.prepareForCompute(list_candlestick, closingArray); // computeProfit(List<TickerItem_CandleBar> candlestickInput, double[]... value)
}

// Runs once for each object in prepareForCompute
function compute(candlestick, ret_outReal) {
    var InitialPrimaryCurrency = bt.getInitialPrimaryCurrency();
    var InitialSecondaryCurrency = bt.getInitialSecondaryCurrency();

    var closingPrice = candlestick.getClose();
    
    var MaxH = bt.getHighest(ret_outReal);
    var MinL = bt.getLowest(ret_outReal);

    if (linearRegAngle > thresholdup) {
        bt.appendDebugMessage("[Buy at " + closingPrice + "] BTC/USD. BTC: " + InitialPrimaryCurrency + ", USD: " + InitialSecondaryCurrency)
        bt.buyAmount(candlestick, closingPrice, 999); // price, amount,
    }
    else if (linearRegAngle < thresholddown) {
        bt.appendDebugMessage("[Sell at " + closingPrice + "] BTC/USD. BTC: " + InitialPrimaryCurrency + ", USD: " + InitialSecondaryCurrency);
        bt.sellAmount(candlestick, closingPrice, 999); // price, amount,
    }
    // bt.sentEmail("<email>", "title", "content"); // via sentgrid
}