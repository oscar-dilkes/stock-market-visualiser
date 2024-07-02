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

  // Function to find the index of the given timestamp using binary search
  private static <T extends DataPoint> int findIndex(List<T> data, long targetTimestamp) {
    int low = 0;
    int high = data.size() - 1;

    while (low <= high) {
      int mid = (low + high) / 2;
      long midTimestamp = data.get(mid).getTimestamp();

      if (midTimestamp < targetTimestamp) {
        low = mid + 1;
      } else if (midTimestamp > targetTimestamp) {
        high = mid - 1;
      } else {
        return mid;
      }
    }

    return low; // If not found, return the insertion point
  }

  public static <T extends DataPoint> List<T> getStockDataInRange(List<T> allData, long startTimestamp, long endTimestamp) {
    int startIndex = findIndex(allData, startTimestamp);
    int endIndex = findIndex(allData, endTimestamp);

    // Ensure the endIndex is within bounds
    if (endIndex < allData.size() && allData.get(endIndex).getTimestamp() > endTimestamp) {
      endIndex--;
    }

    // Ensure endIndex does not exceed the list size
    if (endIndex >= allData.size()) {
      endIndex = allData.size() - 1;
    }

    // Extract the sublist
    if (startIndex <= endIndex) {
      return new ArrayList<>(allData.subList(startIndex, endIndex + 1));
    } else {
      return new ArrayList<>(); // Return an empty list if the range is invalid
    }
  }

}
