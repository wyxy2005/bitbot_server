package _client.viewmodelitem;


/**
 *
 * @author twili
 */
public class TradesViewModelItem   {

    private long _TimeMillis;
    private double _Price, _Amount;
    private final int _TradeId;
    private final TradesType _Type;

    public TradesViewModelItem(long _TimeMillis, double _Price, double _Amount, int _TradeId, TradesType _Type) {
        this._TimeMillis = _TimeMillis;
        this._Price = _Price;
        this._Amount = _Amount;
        this._TradeId = _TradeId;
        this._Type= _Type;
    }

    public int getTradeId() {
        return _TradeId;
    }

    public TradesType getTradesType() {
        return _Type;
    }

    public double getPrice() {
        return _Price;
    }
    public void setPrice(double _Price) {
       this._Price = _Price;
    }

    public double getAmount() {
        return _Amount;
    }
    public void setAmount(double _Amount) {
        this._Amount = _Amount;
    }

    public long getTimeMillis() {
        return _TimeMillis;
    }
    public void setTimeMillis(long _TimeMillis) {
        this._TimeMillis = _TimeMillis;
    }
}
