package _client.apis;

public class ReturnData_Ticker {

    private double high;
    private double low;
    private double vol;
    private double vol_cur;
    private double last;
    private double buy;
    private double sell;
    private double avg;
    private long server_time;

    public ReturnData_Ticker(double high, double low, double vol, double vol_cur, double last, double buy, double sell, long server_time) {
        this.high = high;
        this.low = low;
        this.vol = vol;
        this.vol_cur = vol_cur;
        this.last = last;
        this.buy = buy;
        this.sell = sell;
        this.server_time = server_time;
        this.avg = (high + low) / 2;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getAvg() {
        return avg;
    }
    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getVol(){
        return vol;
    }

    public double getVolCur() {
        return vol_cur;
    }

    public double getLast() {
        return last;
    }

    public double getBuy() {
        return buy;
    }

    public double getSell() {
        return sell;
    }

    public long getServerTime() {
        return server_time;
    }
}
