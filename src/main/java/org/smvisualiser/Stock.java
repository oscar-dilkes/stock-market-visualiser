package org.smvisualiser;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Set;

public class Stock {
  private final String ticker;
  private final List<StockDataPoint> stockDataPoints;
  private final boolean retrievalSuccess;

  public Stock(String ticker, JsonObject data, boolean retrievalSuccess) {
    this.ticker = ticker;
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

  public List<StockDataPoint> getStockDataPoints() {
    return this.stockDataPoints;
  }

  public boolean isRetrievalSuccess() {
    return this.retrievalSuccess;
  }
}
