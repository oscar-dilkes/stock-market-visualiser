package org.smvisualiser;

public class StockDataPoint extends DataPoint {

  private final double openPrice;
  private final double closePrice;
  private final double highPrice;
  private final double lowPrice;
  private final long volume;

  public StockDataPoint(long timestamp, double openPrice, double closePrice, double highPrice, double lowPrice, long volume) {
    super(timestamp);
    this.openPrice = openPrice;
    this.closePrice = closePrice;
    this.highPrice = highPrice;
    this.lowPrice = lowPrice;
    this.volume = volume;
  }

  public double getOpenPrice() {
    return openPrice;
  }

  public double getClosePrice() {
    return closePrice;
  }

  public double getHighPrice() {
    return highPrice;
  }

  public double getLowPrice() {
    return lowPrice;
  }

  public long getVolume() {
    return volume;
  }

}
