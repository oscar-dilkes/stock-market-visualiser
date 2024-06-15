package org.smvisualiser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.*;


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

  public Stock retrieveData(String ticker, int multiplier, String timespan, String from, String to) throws IOException {

    String url = String.format("%s/v2/aggs/ticker/%s/range/%d/%s/%s/%s?apiKey=%s",
            BASE_URL, ticker, multiplier, timespan, from, to, API_KEY);

    Request request = new Request.Builder()
            .url(url)
            .build();

    try(Response response = httpClient.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        return new Stock(ticker, null, false);
      }
      else {
        String responseBody = response.body().string();
        JsonObject data = gson.fromJson(responseBody, JsonObject.class);
        return new Stock(ticker, data, true);
      }
    }

  }
}
