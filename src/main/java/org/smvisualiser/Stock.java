package org.smvisualiser;

import kong.unirest.json.JSONObject;

public class Stock {

  private final String symbol;
  private JSONObject data;

  public Stock(String symbol) {
    this.symbol = symbol;
  }

  public JSONObject getData() {
    return data;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setData(JSONObject data) {
    this.data = data;
  }

}
