package org.smvisualiser;

import com.google.gson.JsonObject;

import java.util.List;

public class Stock {
  private final String ticker;
  private final String name;
  private final String sector;

  private List<StockDataPoint> stockDataPoints;
  private boolean retrievalSuccess = false;

  private List<RSIValue> rsiValues;
  private boolean rsiCalculated = false;

  public Stock(String ticker, String name, String sector) {
    this.ticker = ticker;
    this.name = name;
    this.sector = sector;
  }

  public void setStockDataPoints (JsonObject data, boolean retrievalSuccess) {
    System.out.println(data);
    this.retrievalSuccess = retrievalSuccess;
    if (retrievalSuccess) {
      this.stockDataPoints = StockDataParser.parseHistoricalDataPoints(data);
    }
    else {
      this.stockDataPoints = null;
    }
  }

  public void setRsiValues(List<RSIValue> rsiValues) {
    this.rsiCalculated = true;
    this.rsiValues = rsiValues;
  }

  public String getTicker() {
    return this.ticker;
  }

  public String getName() {
    return this.name;
  }

  public String getSector() {
    return this.sector;
  }

  public List<StockDataPoint> getStockDataPoints() {
    return this.stockDataPoints;
  }

  public boolean isRetrievalSuccess() {
    return this.retrievalSuccess;
  }

  public List<RSIValue> getRsiValues() {
    return this.rsiValues;
  }

  public boolean isRsiCalculated() {
    return rsiCalculated;
  }

  @Override
  public String toString() {
    // Return what you want to display in the JComboBox
    return ticker; // Display the ticker symbol
  }
}
