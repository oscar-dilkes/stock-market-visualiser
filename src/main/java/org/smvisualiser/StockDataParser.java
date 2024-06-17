package org.smvisualiser;

import com.formdev.flatlaf.json.Json;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class StockDataParser {

  public static List<StockDataPoint> parseHistoricalDataPoints(JsonObject historicalData) {
    List<StockDataPoint> historicalDataPoints = new ArrayList<>();

    if (historicalData.has("results")) {
      JsonArray results = historicalData.getAsJsonArray("results");

      for (JsonElement result : results) {
        JsonObject dataPoint = result.getAsJsonObject();

        long timestamp = dataPoint.get("t").getAsLong();
        double openPrice = dataPoint.get("o").getAsDouble();
        double closePrice = dataPoint.get("c").getAsDouble();
        double highPrice = dataPoint.get("h").getAsDouble();
        double lowPrice = dataPoint.get("l").getAsDouble();
        long volume = dataPoint.get("v").getAsLong();

        historicalDataPoints.add(new StockDataPoint(timestamp, openPrice, closePrice, highPrice, lowPrice, volume));
      }
    }
    else {
      return null;
    }
    return historicalDataPoints;
  }

}
