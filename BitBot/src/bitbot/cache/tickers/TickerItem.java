package bitbot.cache.tickers;


/**
 *
 * @author z
 */
public interface TickerItem {
    
    public long getRealServerTime();
    
    public long getServerTime();

    public float getHigh();

    public float getLow();

    public float getOpen();
    
    public float getClose();

    public double getVol();

    public double getVol_Cur();
    

    // set
    
    public void setVol(double value);

    public void setVol_Cur(double value);
    
    public void setHigh(float value);

    public void setLow(float value);

    public void setOpen(float value);
    
    public void setClose(float value);
}
