package org.smvisualiser;

import com.google.gson.JsonObject;

import java.util.List;

public class Stock {
  private final String ticker;
  private final String name;
  private final String sector;

  private List<StockDataPoint> stockDataPoints;
  private boolean retrievalSuccess;

  public Stock(String ticker, String name, String sector) {
    this.ticker = ticker;
    this.name = name;
    this.sector = sector;
  }

  public void setStockDataPoints (JsonObject data, boolean retrievalSuccess) {
    this.retrievalSuccess = retrievalSuccess;
    if (retrievalSuccess) {
      this.stockDataPoints = StockDataParser.parseHistoricalDataPoints(data);
    }
    else {
      this.stockDataPoints = null;
    }
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

  @Override
  public String toString() {
    // Return what you want to display in the JComboBox
    return ticker; // Display the ticker symbol
  }
}
