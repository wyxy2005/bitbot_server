/*
 * TaLib documentation: http://www.heatonresearch.com/content/technical-analysis-library-ta-lib-tutorial-java
 * Java <> JS scripting guide: http://docs.oracle.com/javase/7/docs/technotes/guides/scripting/programmer_guide/
*/

var period_hour = 4;
var optInTimePeriod = 20;
var thresholdup = 30;
var thresholddown = 5;

function initialize() {
}

function handle() {

    var talib = bt.getTaLibCore();
    var closingArray = bt.getClosingPriceArray(
        "btc_usd",
        (Math.max(5, (period_hour * 60) * 20) / 5),
        period_hour * 60,
        "btce",
        0); // String ticker, int backtestHours, int intervalMinutes, String ExchangeSite, long ServerTimeFrom

    // startIdx, endIdx, inReal, optInTimePeriod, null, null, outReal
    var ret_outBegIdx = bt.createMInteger();
    var ret_outNBElement = bt.createMInteger();
    var ret_outReal = bt.createDoubleArray(closingArray.length); // use createDoubleArray, don't allow creation of java.lang.Double due to sandbox
    var retCode = talib.linearRegAngle(0, closingArray.length - 1, closingArray, optInTimePeriod, ret_outBegIdx, ret_outNBElement, ret_outReal);

    // java.lang.System.out.println(ret_outReal[0]);

    var linearRegAngleNow = ret_outReal[ret_outReal.length - 1];
    if (linearRegAngleNow > thresholdup)
    {
        buy(closingArray[closingArray.length - 1], 999); // price, amount,
    }
    else if (linearRegAngleNow < thresholddown) {
        sell(closingArray[closingArray.length - 1], 999); // price, amount,
    }
    // sentEmail("<email>", "title", "content"); // via sentgrid
}