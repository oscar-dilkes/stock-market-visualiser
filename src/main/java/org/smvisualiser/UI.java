package org.smvisualiser;

import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultHighLowDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

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
    chartPanel.setPreferredSize(new Dimension(800, 600)); // Set preferred size
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

          XYPlot plot = (XYPlot) chart.getPlot();
          ValueAxis xAxis = plot.getDomainAxis();
          xAxis.setTickLabelsVisible(false);  // Hide x-axis labels

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
    List dataPoints = thisStock.getStockDataPoints();

    Date[] date = new Date[dataPoints.size()];
    double[] high = new double[dataPoints.size()];
    double[] low = new double[dataPoints.size()];
    double[] open = new double[dataPoints.size()];
    double[] close = new double[dataPoints.size()];
    double[] volume = new double[dataPoints.size()];

    for (int i = 0; i < dataPoints.size(); i++) {
      StockDataPoint dataPoint = (StockDataPoint) dataPoints.get(i);
      date[i] = new Date(dataPoint.getTimestamp());
      high[i] = dataPoint.getHighPrice();
      low[i] = dataPoint.getLowPrice();
      open[i] = dataPoint.getOpenPrice();
      close[i] = dataPoint.getClosePrice();
      volume[i] = dataPoint.getVolume();
    }

    DefaultHighLowDataset dataset = new DefaultHighLowDataset(thisStock.getTicker(), date, high, low, open, close, volume);
    return ChartFactory.createCandlestickChart(
            "",
            "Date",
            "Price",
            dataset,
            false
    );
  }
}