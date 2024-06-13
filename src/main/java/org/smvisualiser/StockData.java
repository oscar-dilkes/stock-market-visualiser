package org.smvisualiser;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.apache.http.util.Asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Set;


public class StockData {

  private static final String BASE_URL = "https://www.alphavantage.co/query";

  public static void main(String[] args) throws IOException {

    String avKey = Files.readString(Paths.get("av_key")).trim();

    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

    System.out.println("Please enter the desired stock symbol: ");

    String symbol = consoleReader.readLine();

    HttpResponse<JsonNode> response = Unirest.get(BASE_URL)
            .queryString("function", "TIME_SERIES_DAILY")
            .queryString("symbol", symbol)
            .queryString("apikey", avKey)
            .asJson();
    if (response.isSuccess()) {
      JSONObject stockData = response.getBody().getObject();
//      System.out.println(stockData.toString(4));
      JSONObject timeSeries = stockData.getJSONObject("Time Series (Daily)");
      Set<String> dates = timeSeries.keySet();
      for (String date : dates) {
        System.out.println(date + ": High = " + timeSeries.getJSONObject(date).get("2. high"));
      }
    }
    else {
      System.out.println("Error: " + response.getStatusText());
    }
  }
}
