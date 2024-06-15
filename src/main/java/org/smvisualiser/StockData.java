package org.smvisualiser;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.util.Set;


public class StockData {

  public static Stock retrieveData(String symbol) throws IOException {

    Stock stock = YahooFinance.get(symbol);

    return stock;
  }
}
