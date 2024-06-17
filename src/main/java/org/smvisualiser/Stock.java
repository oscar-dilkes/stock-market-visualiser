package org.smvisualiser;

import com.google.gson.JsonObject;

import java.util.List;

public class Stock {
  private final String ticker;
  private final String name;
  private final String market;
  private final String locale;
  private final String primaryExchange;
  private final String type;
  private final String currency;

  private List<StockDataPoint> stockDataPoints;
  private boolean retrievalSuccess;

  public Stock(String ticker, String name, String market, String locale, String primaryExchange, String type, String currency) {
    this.ticker = ticker;
    this.name = name;
    this.market = market;
    this.locale = locale;
    this.primaryExchange = primaryExchange;
    this.type = type;
    this.currency = currency;
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
    return name;
  }

  public String getMarket() {
    return market;
  }

  public String getLocale() {
    return locale;
  }

  public String getPrimaryExchange() {
    return primaryExchange;
  }

  public String getType() {
    return type;
  }

  public String getCurrency() {
    return currency;
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
