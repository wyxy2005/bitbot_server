package _client.apis;

import _client.charts.MarketDepth;
import java.util.ArrayList;
import java.util.List;

public class ReturnData_Depth {
    public List<MarketDepth> asks = new ArrayList<>();
    public List<MarketDepth> bids = new ArrayList<>();
    public List<MarketDepth> formattedDepth = new ArrayList<>();

    public double HighVolume = 0;
    public double LowVolume = Integer.MAX_VALUE;
    public double HighCost = 0;
    public double LowCost = Integer.MAX_VALUE;
    public double TotalVolume = 0;
    public double TotalBuy = 0;
    public double TotalSell = 0;

    public double _HighestBuy = 0;
    public double _HighestSell = Integer.MAX_VALUE;
    public double _Spread = 0;
    public float _buyRatio = 0, _sellRatio = 0;

    public void ProcessData() {
        int FilterWhalesThreshold = 0; // ApplicationSetting.GraphDensity_OrderBook_FilerBuy;

        ///// Buys
        double totalBuysAmount = 0;
        for (int i = bids.size() - 1; i > 0; i--)
        {
            MarketDepth item = bids.get(i);

            if (item.getCost() <= _HighestBuy)
            {
                // check for whales
                double thisVolume = item.getAmount() * item.getCost();
                double difference = thisVolume / TotalVolume * 100d;
                if (FilterWhalesThreshold != 0 && difference > FilterWhalesThreshold)
                {
                    item.setCost(0);
                    item.setCumulativeVolume(0);
                    continue;
                }
                formattedDepth.add(item);
            }
        }
        for (int i = formattedDepth.size() - 1; i > 0; i--)
        {
            MarketDepth item = formattedDepth.get(i);

            totalBuysAmount += item.getAmount();
            item.setAmount(totalBuysAmount);
        }


        ///// Sells
        double totalSellsAmount = 0;
        //data._formatted_data_tmpSell.Sort(p => p.Cost);
        for (int i = 0; i < asks.size(); i++)
        {
            MarketDepth item = asks.get(i);

            if (item.getCost() >= _HighestSell)
            {
                // check for whales
                double thisVolume = item.getAmount() * item.getCost();
                double difference = thisVolume / TotalVolume * 100d;
                if (FilterWhalesThreshold != 0 && difference > FilterWhalesThreshold)
                {
                    item.setCost(0);
                    item.setCumulativeVolume(0);
                    continue;
                }

                totalSellsAmount += item.getAmount();
                item.setAmount(totalSellsAmount);

                formattedDepth.add(item);
            }
        }
        double buy1 = TotalBuy;
        double sell1 = TotalSell;

        if (sell1 > buy1)
        {
            sell1 = Math.round((sell1 / buy1) *100.0)/100.0;
            buy1 = 1;
        }
        else
        {
            buy1 = Math.round((buy1 / sell1) *100.0)/100.0;
            sell1 = 1;
        }
        _Spread = _HighestSell - _HighestBuy;
        _buyRatio = (float) buy1;
        _sellRatio = (float) sell1;

        // Cleanup data
        bids.clear();
        asks.clear();
    }

    public void UpdateMaxMinCostVolumeData(double volume, double cost, boolean isBuy)
    {
        if (LowVolume > volume) {
            LowVolume = volume;
        }
        if (HighVolume < volume) {
            HighVolume = volume;
        }
        if (LowCost > cost) {
            LowCost = cost;
        }
        if (HighCost < cost) {
            HighCost = cost;
        }
        TotalVolume += volume * cost;

        if (isBuy) {
            if (_HighestBuy < cost)
                _HighestBuy = cost;

            // for Ratio
            TotalBuy += (volume * cost);
        } else {
            if (_HighestSell > cost)
                _HighestSell = cost;

            // for Ratio
            TotalSell += (volume * cost);
        }
    }
}
