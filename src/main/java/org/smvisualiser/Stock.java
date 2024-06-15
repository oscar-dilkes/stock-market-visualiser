package org.smvisualiser;

import com.google.gson.JsonObject;

public class Stock {
  private final String ticker;
  private final JsonObject data;
  private final boolean retrievalSuccess;

  public Stock(String ticker, JsonObject data, boolean retrievalSuccess) {
    this.ticker = ticker;
    this.data = data;
    this.retrievalSuccess = retrievalSuccess;
  }

  public String getTicker() {
    return this.ticker;
  }

  public JsonObject getData() {
    return this.data;
  }

  public boolean isRetrievalSuccess() {
    return this.retrievalSuccess;
  }
}
