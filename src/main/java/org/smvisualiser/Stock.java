package org.smvisualiser;

import com.google.gson.JsonObject;

import java.util.Set;

public class Stock {
  private final String ticker;
  private final Set<StockDataPoint> stockDataPoints;
  private final boolean retrievalSuccess;

  public Stock(String ticker, JsonObject data, boolean retrievalSuccess) {
    this.ticker = ticker;
    this.stockDataPoints = StockDataParser.parseHistoricalDataPoints(data);
    this.retrievalSuccess = retrievalSuccess;
  }

  public String getTicker() {
    return this.ticker;
  }

  public Set<StockDataPoint> getStockDataPoints() {
    return this.stockDataPoints;
  }

  public boolean isRetrievalSuccess() {
    return this.retrievalSuccess;
  }
}
