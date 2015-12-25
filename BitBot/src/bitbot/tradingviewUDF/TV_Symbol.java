package bitbot.tradingviewUDF;

/**
 *
 * @author zheng
 */
public class TV_Symbol {

    private final String name, description, exchange, type;
    private final TVSymbolType symbolType;
    private final String secondaryInternalEntryName;

    public TV_Symbol(String name_, String description_, String exchange_, String type_) {
        this(name_, description_, exchange_, type_, "");
    }
    
    public TV_Symbol(String name_, String description_, String exchange_, String type_, String secondaryInternalEntryName_) {
        this.name = name_;
        this.description = description_;
        this.exchange = exchange_;
        this.type = type_;
        this.symbolType = name_.endsWith("VOL") ? TVSymbolType.CumulativeVolume : name_.startsWith("SWAPS") ? TVSymbolType.Swaps : TVSymbolType.Price;
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

    public TVSymbolType getSymbolType() {
        return symbolType;
    }

    public TV_Symbol cloneObject() {
        TV_Symbol sym = new TV_Symbol(name, description, exchange, type);

        return sym;
    }
}
