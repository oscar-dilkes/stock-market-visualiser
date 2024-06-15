package org.smvisualiser;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UI {
  public static void display() {
    SwingUtilities.invokeLater(UI::createAndShowGUI);
  }

  public static String unixTimestampConverter(long unixTimestamp) {
    Instant instant = Instant.ofEpochMilli(unixTimestamp);
    LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    return dateTime.format(formatter);
  }

  public static void createAndShowGUI() {
    JFrame frame = new JFrame("HW");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(10,10,10,10));

    JPanel inputPanel = new JPanel(new FlowLayout());
    JLabel instructionLabel = new JLabel("Enter Symbol:");
    JTextField tickerField = new JTextField(10);
    JButton submitButton = new JButton("Submit");

    inputPanel.add(instructionLabel);
    inputPanel.add(tickerField);
    inputPanel.add(submitButton);

    mainPanel.add(inputPanel);

    JPanel chartPanel = new JPanel(new BorderLayout());

    mainPanel.add(chartPanel);

    submitButton.addActionListener(e -> {
      String ticker = tickerField.getText().trim();
      if (!ticker.isEmpty()) {
        try {
          String from = "2024-04-14";
          String to = "2024-06-14";
          PolygonClient client = new PolygonClient();
          Stock thisStock = client.retrieveData(ticker, 1, "day", from, to);

          JFreeChart chart = createLineChart(thisStock, from, to);
          ChartPanel chartPanelComponent = new ChartPanel(chart);

          chartPanelComponent.setPreferredSize(new Dimension(800, 600));

          chartPanel.removeAll();
          chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
          chartPanel.revalidate();
          chartPanel.repaint();

          frame.pack();

        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      }
      else {
        JOptionPane.showMessageDialog(frame, "Please enter a symbol.");

      }
    });

    frame.getContentPane().add(mainPanel);
    frame.pack();
    frame.setVisible(true);
  }

  public static JFreeChart createLineChart(Stock thisStock, String from, String to) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();

    for (StockDataPoint stockDataPoint : thisStock.getStockDataPoints()) {
      dataset.addValue(stockDataPoint.getClosePrice(), "Close Price", unixTimestampConverter(stockDataPoint.getTimestamp()));
    }

    return ChartFactory.createLineChart(
            "Close Price from " + from + " to " + to,
            "Date",
            "Close Price",
            dataset);
  }
}