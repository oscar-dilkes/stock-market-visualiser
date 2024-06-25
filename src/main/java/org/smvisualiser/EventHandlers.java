package org.smvisualiser;

import com.toedter.calendar.JDateChooser;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventHandlers {
  public void handleSubmit(JFrame frame, JComboBox<Stock> stockBox, JDateChooser startDateChooser, JDateChooser endDateChooser, JCheckBox showMA, JTextField periodLengthField, JPanel chartPanel) throws IOException {
    PolygonClient client = new PolygonClient();

    StockChartFactory stockChartFactory = new StockChartFactory();

    if (UI.requestTimestamps.size() >= 5) {
      JOptionPane.showMessageDialog(frame, "Request limit exceeded: Try again once counter goes below 5.");
      return;
    }
    long currentTime = System.currentTimeMillis();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    Date currentDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.add(Calendar.YEAR, -2);
    Date twoYearsPriorDate = calendar.getTime();

    String twoYearsPrior = sdf.format(twoYearsPriorDate);
    String today = sdf.format(currentDate);

    Stock stock = (Stock) stockBox.getSelectedItem();
    Date startDate = startDateChooser.getDate();
    Date endDate = endDateChooser.getDate();

    if (startDate == null || endDate == null) {
      JOptionPane.showMessageDialog(frame, "Please select both start and end dates.");
      return;
    }

    try {
      // If retrieval hasn't been attempted yet then boolean will be false, so in that case, retrieve data
      // Saves double retrieval
      if (!stock.isRetrievalSuccess()) {
        client.retrieveStockData(stock, twoYearsPrior, today);
        UI.requestTimestamps.add(currentTime);
        UI.updateCountLabel();
      }

      // If retrieval is still unsuccessful then display error
      if (!stock.isRetrievalSuccess()) {
        JOptionPane.showMessageDialog(frame, "Data retrieval error: Stock may be unavailable.");
        return;
      }

      int periodLengthValue;

      boolean showMa = UI.isMa.get();
      if (showMa) {
        String periodString = periodLengthField.getText();
        if (periodString.equalsIgnoreCase("")) {
          JOptionPane.showMessageDialog(frame, "Please give a period length for moving average.");
          return;
        }
        periodLengthValue = Integer.parseInt(periodString.trim());
      } else {
        periodLengthValue = 0;
      }

      JFreeChart chart = stockChartFactory.createChart(stock, startDate, endDate, showMa, periodLengthValue, frame);

      ChartPanel chartPanelComponent = UI.getChartPanel(chart);
      chartPanel.removeAll();
      chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
      chartPanel.revalidate();
      chartPanel.repaint();
      frame.pack();

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public List<Stock> handleIndexBox(JFrame frame, JComboBox<Index> indexBox, JComboBox<Stock> stockBox) {
    IndexWikipediaScraper scraper = new IndexWikipediaScraper();
    List<Stock> stockList;
    stockBox.removeAllItems();
    stockList = scraper.getStockList((Index) indexBox.getSelectedItem());
    if (stockList != null) {
      for (Stock stock : stockList) {
        stockBox.addItem(stock);
      }
    } else {
      JOptionPane.showMessageDialog(frame, "Please select a panel.");
    }
    return stockList;
  }
}

