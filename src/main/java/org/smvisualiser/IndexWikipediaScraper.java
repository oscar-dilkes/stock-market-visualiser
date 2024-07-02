package org.smvisualiser;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IndexWikipediaScraper {
  List<Stock> stockList;

  private List<Stock> scraper(String url) {
    String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
    stockList = new ArrayList<>();
    try {
      Document doc = Jsoup.connect(decodedUrl).get();
      Element table = doc.getElementById("constituents");

      if (table != null) {
        Elements rows = table.select("tr");

        int tickerColumnIndex = -1;
        int nameColumnIndex = -1;
        int categoryColumnIndex = -1;

        Elements headers = rows.get(0).select("th");
        for (int i = 0; i < headers.size(); i++) {
          String headerText = headers.get(i).text().toLowerCase();
          if (headerText.contains("ticker") || headerText.contains("symbol")) {
            tickerColumnIndex = i;
          } else if (headerText.contains("security") || headerText.contains("company")) {
            nameColumnIndex = i;
          } else if (headerText.contains("sector")) {
            categoryColumnIndex = i;
          }
        }

        for(int i = 1; i < rows.size(); i++) {
          Element row = rows.get(i);
          Elements cols = row.select("td");
          if (!cols.isEmpty()) {
            stockList.add(new Stock(cols.get(tickerColumnIndex).text(), cols.get(nameColumnIndex).text(), cols.get(categoryColumnIndex).text()));
          }
        }
      } else {
        System.out.println("Table not found.");
      }
    } catch (IOException ex) {
      return null;
    }
    return stockList;
  }

  public List<Stock> getStockList(Index index) {
    if (index != null) {
      return scraper(index.getWikipediaUrl());
    }
    return null;
  }
}
