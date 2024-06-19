package org.smvisualiser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DataProcessor {

  public List<RSIValue> RSICalculator (List<StockDataPoint> dataPoints) {
    List<RSIValue> rsiValues = new ArrayList<>();

    double initialGains = 0;
    double initialLoss = 0;
    for (int i = 0; i < 14; i++) {
      StockDataPoint dataPoint = dataPoints.get(i);
      double value = dataPoint.getClosePrice() - dataPoint.getOpenPrice();
      if (value > 0) {
        initialGains += value;
      } else {
        value *= -1;
        initialLoss += value;
      }
      rsiValues.add(new RSIValue(Double.NaN, dataPoint.getTimestamp()));
    }

    double averageGain = initialGains/14;
    double averageLoss = initialLoss/14;

    double rsi;

    rsiValues.add(new RSIValue(Double.NaN, dataPoints.get(14).getTimestamp()));

    double prevAvGain = averageGain;
    double prevAvLoss = averageLoss;

    double currentGain = 0;
    double currentLoss = 0;

    double rs;

    double priceChange;

    for (int i = 15; i < dataPoints.size(); i++) {
      currentGain = 0;
      currentLoss = 0;
      StockDataPoint dataPoint = dataPoints.get(i);
      priceChange = dataPoint.getClosePrice() - dataPoint.getOpenPrice();
      if (priceChange > 0) {
        currentGain = priceChange;
      } else {
        currentLoss = priceChange * -1;
      }

      averageGain = ((prevAvGain * 13) + currentGain)/14;
      averageLoss = ((prevAvLoss * 13) + currentLoss)/14;

      rs = averageGain/averageLoss;

      rsi = 100 - (100/(1 + rs));

      rsiValues.add(new RSIValue(rsi, dataPoint.getTimestamp()));

      prevAvGain = averageGain;
      prevAvLoss = averageLoss;
    }

    return rsiValues;
  }

  public List<MAValue> SMACalculator (List<StockDataPoint> dataPoints, int duration) {
    List<MAValue> maValues = new ArrayList<>();

    double initialVal = dataPoints.get(0).getClosePrice();
    double maDividend = initialVal;

    int x = Math.min(duration, dataPoints.size());

    for (int i = 1; i < x; i++) {
      maDividend += dataPoints.get(i).getClosePrice();
    }

    for (int i = duration; i < dataPoints.size(); i++) {
      maDividend = maDividend - initialVal + dataPoints.get(i).getClosePrice();
      initialVal = dataPoints.get(i - duration).getClosePrice();
      maValues.add(new MAValue(maDividend/duration, dataPoints.get(i).getTimestamp()));
    }

    return maValues;

  }

  // to find from date in data for displaying candlesticks, when adding
  // the data by parsing json, I should identify when im at the from date and store the index
  // write a cool algorithm to do this

  // collect 2 years historical data
  // update submit button counter to be a request counter
}
