package bitbot.tickers;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.simple.JSONObject;

/**
 *
 * @author z
 */
public class TickerItemData implements TickerItem {

    private final long server_time, updated;
    private float high, low, avg, buy, sell, last;
    private double vol, vol_cur;

    public TickerItemData(ResultSet rs) throws SQLException {
        this.server_time = rs.getLong("server_time");//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.updated = rs.getLong("updated");//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("updated").toString()));
        this.high = rs.getFloat("high");
        this.low = rs.getFloat("low");
        this.avg = rs.getFloat("avg");
        this.buy = rs.getFloat("buy");
        this.sell = rs.getFloat("sell");
        this.last = rs.getFloat("last");
        this.vol = rs.getDouble("vol");
        this.vol_cur = rs.getDouble("vol_cur");

        //System.out.println("server_time at: " + server_time.toString());
        //System.out.println("updated at: " + updated.toString());
    }
    
    public TickerItemData(JSONObject obj) {
        this.server_time = Long.parseLong(obj.get("server_time").toString());//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("server_time").toString()));
        this.updated = Long.parseLong(obj.get("updated").toString());//DateTimeUtil.convertDateTime(Long.parseLong(obj.get("updated").toString()));
        this.high = Float.parseFloat(obj.get("high").toString());
        this.low = Float.parseFloat(obj.get("low").toString());
        this.avg = Float.parseFloat(obj.get("avg").toString());
        this.buy = Float.parseFloat(obj.get("buy").toString());
        this.sell = Float.parseFloat(obj.get("sell").toString());
        this.last = Float.parseFloat(obj.get("last").toString());
        this.vol = Double.parseDouble(obj.get("vol").toString());
        this.vol_cur = Double.parseDouble(obj.get("vol_cur").toString());

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
    public long getUpdated() {
        return updated;
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
    public float getAvg() {
        return avg;
    }

    @Override
    public float getBuy() {
        return buy;
    }

    @Override
    public float getSell() {
        return sell;
    }

    @Override
    public float getLast() {
        return last;
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
    public void setAvg(float value) {
        this.avg = value;
    }

    @Override
    public void setBuy(float value) {
        this.buy = value;
    }

    @Override
    public void setSell(float value) {
        this.sell = value;
    }
}
