package org.smvisualiser;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;


public class StockData {
  public static void main(String[] args) throws IOException {

    Stock stock = YahooFinance.get("AAPL");

    BigDecimal price = stock.getQuote().getPrice();
    BigDecimal change = stock.getQuote().getChangeInPercent();
    BigDecimal peg = stock.getStats().getPeg();
    BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();

    System.out.println("Symbol: " + stock.getSymbol());
    System.out.println("Price: " + price);
    System.out.println("Change: " + change);
    System.out.println("PEG: " + peg);
    System.out.println("Dividend: " + dividend);


  }
}
