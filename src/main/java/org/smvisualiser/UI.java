package org.smvisualiser;

import com.formdev.flatlaf.FlatLightLaf;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.LinkedList;
import javax.swing.Timer;
import java.awt.event.ActionListener;



public class UI {

  private static JLabel countLabel;
  private static LinkedList<Long> pressTimestamps = new LinkedList<>();
  private static final Font FONT = new JLabel().getFont();


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
    FlatLightLaf.setup();
    JFrame frame = new JFrame("Stock Market Visualiser");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(10,10,10,10));

    JPanel inputPanel = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL; // Make components fill horizontally
    gbc.weightx = 1.0;


    JLabel instructionLabel = new JLabel("Symbol:");

    JTextField tickerField = new JTextField();

    JButton submitButton = new JButton("Submit");

    countLabel = new JLabel("Presses in last minute: 0");

    // Start Date Picker Panel
    JPanel startDatePanel = new JPanel();
    JLabel startDateLabel = new JLabel("Start Date");
    UtilDateModel modelStart = new UtilDateModel();
    Properties properties = new Properties();
    properties.put("text.today", "Today");
    properties.put("text.month", "Month");
    properties.put("text.year", "Year");
    JDatePanelImpl datePanelStart = new JDatePanelImpl(modelStart, properties);
    JDatePickerImpl datePickerStart = new JDatePickerImpl(datePanelStart, new DateComponentFormatter());
    startDatePanel.add(startDateLabel);
    startDatePanel.add(datePickerStart);

    // End Date Picker Panel
    JPanel endDatePanel = new JPanel();
    JLabel endDateLabel = new JLabel("End Date");
    UtilDateModel modelEnd = new UtilDateModel();
    JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, properties);
    JDatePickerImpl datePickerEnd = new JDatePickerImpl(datePanelEnd, new DateComponentFormatter());
    endDatePanel.add(endDateLabel);
    endDatePanel.add(datePickerEnd);

    inputPanel.add(instructionLabel, gbc);
    inputPanel.add(tickerField, gbc);
    inputPanel.add(startDatePanel, gbc);
    inputPanel.add(endDatePanel, gbc);
    inputPanel.add(submitButton, gbc);
    inputPanel.add(countLabel, gbc);

    mainPanel.add(inputPanel, BorderLayout.WEST);

    // Chart Panel
    JPanel chartPanel = new JPanel(new BorderLayout());
    chartPanel.setPreferredSize(new Dimension(800, 600)); // Set preferred size
    chartPanel.setBackground(Color.WHITE);

    mainPanel.add(chartPanel, BorderLayout.CENTER);

    submitButton.addActionListener(e -> {
      if (pressTimestamps.size() >= 5) {
        JOptionPane.showMessageDialog(frame, "Request limit exceeded: Try again once counter goes below 5.");
        return;
      }
      long currentTime = System.currentTimeMillis();
      pressTimestamps.add(currentTime);
      updateCountLabel();

      String ticker = tickerField.getText().trim();

      Date startDate = (Date) datePickerStart.getModel().getValue();
      Date endDate = (Date) datePickerEnd.getModel().getValue();

      if (startDate == null || endDate == null) {
        JOptionPane.showMessageDialog(frame, "Please select both start and end dates.");
        return;  // Exit the ActionListener if dates are not selected
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      String from = sdf.format(startDate);
      String to = sdf.format(endDate);

      if (!ticker.isEmpty()) {
        try {
          PolygonClient client = new PolygonClient();
          Stock thisStock = client.retrieveData(ticker, 1, "day", from, to);

          if (!thisStock.isRetrievalSuccess()) {
            JOptionPane.showMessageDialog(frame, "Data retrieval error: Ensure ticker symbol exists and date range is valid.");
            return;
          }

          JFreeChart chart = createChart(thisStock, from, to);

          XYPlot plot = (XYPlot) chart.getPlot();
          ValueAxis xAxis = plot.getDomainAxis();
          xAxis.setTickLabelsVisible(false);  // Hide x-axis labels

          ChartPanel chartPanelComponent = getChartPanel(chart);

          chartPanel.removeAll();
          chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
          chartPanel.revalidate();
          chartPanel.repaint();

          frame.pack();

        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      } else {
        JOptionPane.showMessageDialog(frame, "Please enter a symbol.");
      }
    });

    frame.getContentPane().add(mainPanel);
    frame.pack();
    frame.setVisible(true);

    Timer timer = new Timer(1000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateCountLabel();
      }
    });
    timer.start();
  }

  @NotNull
  private static ChartPanel getChartPanel(JFreeChart chart) {
    ChartPanel chartPanelComponent = new ChartPanel(chart);
    chartPanelComponent.setPreferredSize(new Dimension(800, 600));
    chartPanelComponent.setMouseZoomable(false);
    chartPanelComponent.setMouseWheelEnabled(true);
    chartPanelComponent.setDomainZoomable(true);
    chartPanelComponent.setRangeZoomable(false);
    chartPanelComponent.setPreferredSize(new Dimension(1680, 1100));
    chartPanelComponent.setZoomTriggerDistance(Integer.MAX_VALUE);
    chartPanelComponent.setFillZoomRectangle(false);
    chartPanelComponent.setZoomOutlinePaint(new Color(0f, 0f, 0f, 0f));
    chartPanelComponent.setZoomAroundAnchor(true);
    return chartPanelComponent;
  }


  public static JFreeChart createChart(Stock thisStock, String from, String to) {
    List<StockDataPoint> dataPoints = thisStock.getStockDataPoints();

    Date[] date = new Date[dataPoints.size()];
    double[] high = new double[dataPoints.size()];
    double[] low = new double[dataPoints.size()];
    double[] open = new double[dataPoints.size()];
    double[] close = new double[dataPoints.size()];
    double[] volume = new double[dataPoints.size()];

    for (int i = 0; i < dataPoints.size(); i++) {
      StockDataPoint dataPoint = dataPoints.get(i);
      date[i] = new Date(dataPoint.getTimestamp());
      high[i] = dataPoint.getHighPrice();
      low[i] = dataPoint.getLowPrice();
      open[i] = dataPoint.getOpenPrice();
      close[i] = dataPoint.getClosePrice();
      volume[i] = dataPoint.getVolume();
    }

    DefaultHighLowDataset dataset = new DefaultHighLowDataset(thisStock.getTicker(), date, high, low, open, close, volume);
    JFreeChart chart = ChartFactory.createCandlestickChart(
            "Prices and Volume for " + thisStock.getTicker() + " from " + from + " to " + to,
            "Date",
            "Price",
            dataset,
            false
    );

    XYPlot plot = (XYPlot) chart.getPlot();

    Font segoeUIFont = new Font("Segoe UI", Font.PLAIN, 14);
    Font segoeUITitleFont = new Font("Segoe UI", Font.BOLD, 18);

    chart.getTitle().setFont(segoeUIFont);

    return chart;
  }

  private static void updateCountLabel() {
    long currentTime = System.currentTimeMillis();
    long oneMinuteAgo = currentTime - 60000;

    while (!pressTimestamps.isEmpty() && pressTimestamps.getFirst() < oneMinuteAgo) {
      pressTimestamps.removeFirst();
    }

    int count = pressTimestamps.size();

    countLabel.setText("Presses in last minute: " + count);
    if (count >= 5) {
      countLabel.setForeground(Color.RED);
    } else {
      countLabel.setForeground(Color.BLACK);
    }
  }
}