package org.smvisualiser;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;


public class PolygonClient {

  private static final String BASE_URL = "https://api.polygon.io";

  private final String API_KEY;

  private final OkHttpClient httpClient;
  private final Gson gson;

  public PolygonClient() throws IOException {
    this.httpClient = new OkHttpClient();
    this.gson = new Gson();
    this.API_KEY = retrieveKey();
  }

  private static String retrieveKey() throws IOException {
    return Files.readString(Paths.get("polygon_key")).trim();
  }

  public List<Stock> fetchAndParseStocks() throws IOException {
    String url = String.format("%s/v3/reference/tickers?active=true&limit=1000&apiKey=%s",
            BASE_URL, API_KEY);

    Request request = new Request.Builder()
            .url(url)
            .build();
    try(Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        return null;
      }
      else {
        assert response.body() != null;
        String responseBody = response.body().string();
        JsonObject data = gson.fromJson(responseBody, JsonObject.class);
        return StockDataParser.parseStocks(data);
      }
    }
  }

  public void retrieveData(Stock stock, int multiplier, String timespan, String from, String to) throws IOException {

    String url = String.format("%s/v2/aggs/ticker/%s/range/%d/%s/%s/%s?apiKey=%s",
            BASE_URL, stock.getTicker(), multiplier, timespan, from, to, API_KEY);

    Request request = new Request.Builder()
            .url(url)
            .build();

    try(Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        stock.setStockDataPoints(null, false);
      }
      else {
        String responseBody = response.body().string();
        JsonObject data = gson.fromJson(responseBody, JsonObject.class);
        stock.setStockDataPoints(data, true);
      }
    }

  }
}
