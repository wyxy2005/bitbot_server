package bitbot.tradingviewUDF;

/**
 *
 * @author zheng
 */
public class TradingViewSymbol {

    private final String name, description, exchange, type;
    private final TradingViewSymbolType symbolType;
    private final String secondaryInternalEntryName;

    public TradingViewSymbol(String name_, String description_, String exchange_, String type_) {
        this(name_, description_, exchange_, type_, "");
    }
    
    public TradingViewSymbol(String name_, String description_, String exchange_, String type_, String secondaryInternalEntryName_) {
        this.name = name_;
        this.description = description_;
        this.exchange = exchange_;
        this.type = type_;
        this.symbolType = name_.endsWith("VOL") ? TradingViewSymbolType.CumulativeVolume : name_.startsWith("SWAPS") ? TradingViewSymbolType.Swaps : TradingViewSymbolType.Price;
        this.secondaryInternalEntryName = secondaryInternalEntryName_;
    }

    public String getName() {
        return name;
    }
    
    public String getSecondaryInternalEntryName() {
        return secondaryInternalEntryName;
    }

    public String getDescription() {
        return description;
    }

    public String getExchange() {
        return exchange;
    }

    public String getType() {
        return type;
    }

    public TradingViewSymbolType getSymbolType() {
        return symbolType;
    }

    public TradingViewSymbol cloneObject() {
        TradingViewSymbol sym = new TradingViewSymbol(name, description, exchange, type);

        return sym;
    }
}
