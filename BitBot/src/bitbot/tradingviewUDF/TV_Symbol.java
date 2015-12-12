package bitbot.tradingviewUDF;

/**
 *
 * @author zheng
 */
public class TV_Symbol {
    private final String name, description, exchange, type;
    private final boolean HaveBalanceVolume;
    
    public TV_Symbol(String name_, String description_, String exchange_, String type_) {
        this.name = name_;
        this.description = description_;
        this.exchange = exchange_;
        this.type = type_;
        this.HaveBalanceVolume = false;
    }
    
    public TV_Symbol(String name_, String description_, String exchange_, String type_, boolean _VolumeProfileData) {
        this.name = name_;
        this.description = description_;
        this.exchange = exchange_;
        this.type = type_;
        this.HaveBalanceVolume = _VolumeProfileData;
    }
    
    public String GetName() {
        return name;
    }
    
    public String GetDescription() {
        return description;
    }
    
    public String GetExchange() {
        return exchange;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean GetHaveBalanceVolume() {
        return HaveBalanceVolume;
    }
    
    public TV_Symbol cloneObject() {
        TV_Symbol sym = new TV_Symbol(name, description, exchange, type, HaveBalanceVolume);
        
        return sym;
    }
}
