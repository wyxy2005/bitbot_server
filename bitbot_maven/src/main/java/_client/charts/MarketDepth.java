package _client.charts;

/**
 *
 * @author twili
 */
public class MarketDepth {

    private boolean _IsBuy = false;
    private double _Amount = 0;
    private double _CumulativeVolume = 0;
    private double _Cost = 0;

    public MarketDepth(boolean _isBuy, double _Cost, double _Amount, double _CumulativeVolume) {
        this._IsBuy = _isBuy;
        this._Cost = _Cost;
        this._Amount = _Amount;
        this._CumulativeVolume = _CumulativeVolume;
    }

    public boolean getIsBuy() {
        return _IsBuy;
    }

    public double getAmount() {
        return _Amount;
    }

    public double getCumulativeVolume() {
        return _CumulativeVolume;
    }

    public double getFiatCurrencyValue() {
        return _Amount * _Cost;
    }

    public double getCost() {
        return _Cost;
    }

    public void setCost(double _Cost) {
        this._Cost = _Cost;
    }

    public void setCumulativeVolume(double _CumulativeVolume) {
        this._CumulativeVolume = _CumulativeVolume;
    }

    public void setAmount(double _Amount) {
        this._Amount = _Amount;
    }
}
