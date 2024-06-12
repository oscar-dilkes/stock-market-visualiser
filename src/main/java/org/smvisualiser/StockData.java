package org.smvisualiser;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.json.*;

import java.io.IOException;
import java.nio.file.*;


public class StockData {

  private static final String BASE_URL = "https://www.alphavantage.co/query";

  public static void main(String[] args) throws IOException {

    String avKey = Files.readString(Paths.get("av_key")).trim();
    System.out.println(avKey);

    String symbol = "AAPL";
    HttpResponse<JsonNode> response = Unirest.get(BASE_URL)
            .queryString("function", "TIME_SERIES_DAILY")
            .queryString("symbol", symbol)
            .queryString("apikey", avKey)
            .asJson();

  }
}
