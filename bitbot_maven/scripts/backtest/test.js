/*
 * TaLib documentation: http://www.heatonresearch.com/content/technical-analysis-library-ta-lib-tutorial-java
 * Java <> JS scripting guide: http://docs.oracle.com/javase/7/docs/technotes/guides/scripting/programmer_guide/
*/

var period_minute = 15;
var optInTimePeriod = 10;
var thresholdup = 2;
var thresholddown = 0;

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

    // startIdx, endIdx, inReal, optInTimePeriod, null, null, outReal
    var ret_outBegIdx = bt.createMInteger();
    var ret_outNBElement = bt.createMInteger();
    var ret_outReal = bt.createDoubleArray(closingArray.length); // use createDoubleArray, don't allow creation of java.lang.Double due to sandbox
    var retCode = talib.linearRegAngle(0, closingArray.length - 1, closingArray, optInTimePeriod, ret_outBegIdx, ret_outNBElement, ret_outReal);

    // java.lang.System.out.println(ret_outReal[0]);

    bt.prepareForCompute(list_candlestick, ret_outReal); // computeProfit(List<TickerItem_CandleBar> candlestickInput, double[]... value)
}

// Runs once for each object in prepareForCompute
function compute(candlestick, ret_outReal) {
    var InitialPrimaryCurrency = bt.getInitialPrimaryCurrency();
    var InitialSecondaryCurrency = bt.getInitialSecondaryCurrency();

    var closingPrice = candlestick.getClose();
    var linearRegAngle = ret_outReal[0]; // Goes according to the order of the unlimited parameter input from prepareForCompute "double[]... value"

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