package bitbot.tradingviewUDF;

/**
 *
 * @author zheng
 */
public class TV_Symbol {
    public String name, description, exchange, type;
    
    public TV_Symbol(String name_, String description_, String exchange_, String type_) {
        this.name = name_;
        this.description = description_;
        this.exchange = exchange_;
        this.type = type_;
    }
    
    public TV_Symbol cloneObject() {
        TV_Symbol sym = new TV_Symbol(name, description, exchange, type);
        
        return sym;
    }
}
