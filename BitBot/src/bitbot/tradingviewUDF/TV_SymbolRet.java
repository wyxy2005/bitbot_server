package bitbot.tradingviewUDF;

/**
 *
 * @author zheng
 */
public class TV_SymbolRet {
    private TV_Symbol symbol;
    private boolean IsCumulativeVolumeType;
    
    public TV_SymbolRet(TV_Symbol symbol, boolean IsCumulativeVolumeType) {
        this.symbol = symbol;
        this.IsCumulativeVolumeType = IsCumulativeVolumeType;
    }
    
    public TV_Symbol getSymbol() {
        return symbol;
    }
    
    public boolean IsCumulativeVolumeType() {
        return IsCumulativeVolumeType;
    }
}
