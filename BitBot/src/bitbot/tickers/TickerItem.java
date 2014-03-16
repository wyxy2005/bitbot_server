package bitbot.tickers;


/**
 *
 * @author z
 */
public interface TickerItem {
    
    public long getRealServerTime();
    
    public long getServerTime();

    public long getUpdated();

    public float getHigh();

    public float getLow();

    public float getAvg();

    public float getBuy();

    public float getSell();

    public float getLast();

    public double getVol();

    public double getVol_Cur();
    

    // set
    
    public void setVol(double value);

    public void setVol_Cur(double value);
    
    public void setHigh(float value);

    public void setLow(float value);

    public void setAvg(float value);

    public void setBuy(float value);

    public void setSell(float value);
}
