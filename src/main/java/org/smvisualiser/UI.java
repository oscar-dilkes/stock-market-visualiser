package org.smvisualiser;

import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultHighLowDataset;

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
//    FlatLightLaf.setup();
    JFrame frame = new JFrame("HW");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(10,10,10,10));

    // Input Panel
    JPanel inputPanel = new JPanel();
    inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
    inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel instructionLabel = new JLabel("Symbol:");
    instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JTextField tickerField = new JTextField();
    tickerField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

    JButton submitButton = new JButton("Submit");

    countLabel = new JLabel("Presses in last minute: 0");
    countLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
    startDatePanel.add(startDateLabel, BorderLayout.NORTH);
    startDatePanel.add(datePickerStart, BorderLayout.CENTER);

    // End Date Picker Panel
    JPanel endDatePanel = new JPanel();
    JLabel endDateLabel = new JLabel("End Date");
    UtilDateModel modelEnd = new UtilDateModel();
    JDatePanelImpl datePanelEnd = new JDatePanelImpl(modelEnd, properties);
    JDatePickerImpl datePickerEnd = new JDatePickerImpl(datePanelEnd, new DateComponentFormatter());
    endDatePanel.add(endDateLabel, BorderLayout.NORTH);
    endDatePanel.add(datePickerEnd, BorderLayout.CENTER);

    inputPanel.add(instructionLabel);
    inputPanel.add(tickerField);
    inputPanel.add(startDatePanel);
    inputPanel.add(endDatePanel);
    inputPanel.add(submitButton);
    inputPanel.add(countLabel);

    mainPanel.add(inputPanel, BorderLayout.WEST);

    // Chart Panel
    JPanel chartPanel = new JPanel(new BorderLayout());
    chartPanel.setPreferredSize(new Dimension(800, 600)); // Set preferred size
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

          JFreeChart chart = createLineChart(thisStock, from, to);

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


  public static JFreeChart createLineChart(Stock thisStock, String from, String to) {
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
            "",
            "Date",
            "Price",
            dataset,
            false
    );

    XYPlot
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