package bitbot.cache.tickers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;

/**
 *
 * @author z
 */
public class TickerItemData implements TickerItem {

    private long server_time;
    private float high, low, open, close, buysell_ratio;
    private double vol, vol_cur;
    private boolean unmatured_data;

    public TickerItemData(long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, boolean unmatured_data) {
        this.server_time = server_time;//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.vol = volume;
        this.vol_cur = volume_cur;
        this.buysell_ratio = buysell_ratio;
        this.unmatured_data = unmatured_data;

        //System.out.println("server_time at: " + server_time.toString());
        //System.out.println("updated at: " + updated.toString());
    }

    public void replaceUnmaturedData(long server_time, float close, float high, float low, float open, double volume, double volume_cur, float buysell_ratio, boolean unmatured_data) {
        if (unmatured_data) {
            this.server_time = server_time;//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        }
        this.high = high;
        this.low = low;
        this.open = open;
        this.close = close;
        this.vol = volume;
        this.vol_cur = volume_cur;
        this.buysell_ratio = buysell_ratio;
        this.unmatured_data = unmatured_data;

        //System.out.println("server_time at: " + server_time.toString());
        //System.out.println("updated at: " + updated.toString());
    }

    public TickerItemData(ResultSet rs) throws SQLException {
        this.server_time = rs.getLong("server_time");//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.high = rs.getFloat("high");
        this.low = rs.getFloat("low");
        this.open = rs.getFloat("open");
        this.close = rs.getFloat("close");
        this.vol = rs.getDouble("vol");
        this.vol_cur = rs.getDouble("vol_cur");
        this.buysell_ratio = rs.getFloat("buysell_ratio");
        this.unmatured_data = false;

        //System.out.println("server_time at: " + server_time.toString());
        //System.out.println("updated at: " + updated.toString());
    }

    @Deprecated
    public TickerItemData(JSONObject obj) {
        this.server_time = Long.parseLong(obj.get("server_time").toString());//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.high = Float.parseFloat(obj.get("high").toString());
        this.low = Float.parseFloat(obj.get("low").toString());
        this.open = Float.parseFloat(obj.get("open").toString());
        this.close = Float.parseFloat(obj.get("close").toString());
        this.vol = Double.parseDouble(obj.get("vol").toString());
        this.vol_cur = Double.parseDouble(obj.get("vol_cur").toString());
        this.buysell_ratio = Float.parseFloat(obj.get("buysell_ratio").toString());
        this.unmatured_data = false;
        //System.out.println("server_time at: " + server_time.toString());
        //System.out.println("updated at: " + updated.toString());
    }

    @Override
    public long getRealServerTime() {
        return server_time;
    }

    @Override
    public long getServerTime() {
        return server_time;
    }

    @Override
    public float getHigh() {
        return high;
    }

    @Override
    public float getLow() {
        return low;
    }

    @Override
    public float getOpen() {
        return open;
    }

    @Override
    public float getClose() {
        return close;
    }

    @Override
    public double getVol() {
        return vol;
    }

    @Override
    public double getVol_Cur() {
        return vol_cur;
    }

    @Override
    public float getBuySell_Ratio() {
        return buysell_ratio;
    }

    @Override
    public boolean isUnmaturedData() {
        return unmatured_data;
    }

    /*  @Override
     public void setVol(double value) {
     this.vol = value;
     }

     @Override
     public void setVol_Cur(double value) {
     this.vol_cur = value;
     }

     @Override
     public void setHigh(float value) {
     this.high = value;
     }

     @Override
     public void setLow(float value) {
     this.low = value;
     }

     @Override
     public void setOpen(float open) {
     this.open = open;
     }

     @Override
     public void setClose(float close) {
     this.close = close;
     }*/
}
