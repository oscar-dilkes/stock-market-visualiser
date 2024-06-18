package org.smvisualiser;

import com.formdev.flatlaf.FlatIntelliJLaf;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.Timer;


public class UI {

  private static JLabel countLabel;
  private static LinkedList<Long> pressTimestamps = new LinkedList<>();
  private static List<Stock> stockList;
  private static PolygonClient client;

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
    FlatIntelliJLaf.setup();

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

    JLabel indexLabel = new JLabel("Index:");

    JComboBox<Index> indexBox = new JComboBox<>();
    indexBox.addItem(new Index("S&P 500", "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies"));
    indexBox.addItem(new Index("Nasdaq-100", "https://en.wikipedia.org/wiki/Nasdaq-100"));
    indexBox.addItem(new Index("FTSE 100", "https://en.wikipedia.org/wiki/FTSE_100_Index"));

    JLabel symbolLabel = new JLabel("Ticker Symbol:");

    JComboBox<Stock> stockBox = new JComboBox<>();

    Index index = (Index) indexBox.getSelectedItem();
    stockList = UI.getStockList(index);
    assert stockList != null;
    for (Stock stock : stockList) {
      stockBox.addItem(stock);
    }

    indexBox.addActionListener(e -> {
      stockBox.removeAllItems();
      stockList = UI.getStockList((Index) indexBox.getSelectedItem());
      if (stockList != null) {
        for (Stock stock : stockList) {
          stockBox.addItem(stock);
        }
      } else {
        JOptionPane.showMessageDialog(frame, "Please select a panel.");
      }

    });

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

    inputPanel.add(indexLabel, gbc);
    inputPanel.add(indexBox, gbc);
    inputPanel.add(symbolLabel, gbc);
    inputPanel.add(stockBox, gbc);
    inputPanel.add(startDatePanel, gbc);
    inputPanel.add(endDatePanel, gbc);
    inputPanel.add(submitButton, gbc);
    inputPanel.add(countLabel, gbc);

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

      Stock thisStock = (Stock) stockBox.getSelectedItem();

      Date startDate = (Date) datePickerStart.getModel().getValue();
      Date endDate = (Date) datePickerEnd.getModel().getValue();

      if (startDate == null || endDate == null) {
        JOptionPane.showMessageDialog(frame, "Please select both start and end dates.");
        return;  // Exit the ActionListener if dates are not selected
      }

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      String from = sdf.format(startDate);
      String to = sdf.format(endDate);

      Date currentDate = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(currentDate);
      calendar.add(Calendar.YEAR, -2);
      Date twoYearsPriorDate = calendar.getTime();

      String twoYearsPrior = sdf.format(twoYearsPriorDate);
      String today = sdf.format(currentDate);

        try {
          client = new PolygonClient();
          assert thisStock != null;
          client.retrieveData(thisStock, 1, "day", twoYearsPrior, today);

          if (!thisStock.isRetrievalSuccess()) {
            JOptionPane.showMessageDialog(frame, "Data retrieval error: Ensure ticker symbol exists and date range is valid.");
            return;
          }

          sdf = new SimpleDateFormat("dd-MM-yyyy");

//          from = sdf.format(startDate);
//          to = sdf.format(endDate);

          JFreeChart chart = createChart(thisStock, startDate, endDate);

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
    });

    frame.getContentPane().add(mainPanel);
    frame.pack();
    frame.setVisible(true);

    Timer timer = new Timer(1000, e -> updateCountLabel());
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


  public static JFreeChart createChart(Stock thisStock, Date from, Date to) {

    /**
     * Retrieve and substantiate dataset
     */

    List<StockDataPoint> dataPoints = StockDataParser.getStockDataInRange(thisStock.getStockDataPoints(), from.getTime(), to.getTime());

    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

    OHLCSeries ohlcSeries = new OHLCSeries(thisStock.getTicker());
    TimeSeries volumeSeries = new TimeSeries(thisStock.getTicker());

    for (StockDataPoint dataPoint: dataPoints) {
      RegularTimePeriod period = new Day(new Date(dataPoint.getTimestamp()));
      ohlcSeries.add(period,
              dataPoint.getOpenPrice(),
              dataPoint.getHighPrice(),
              dataPoint.getLowPrice(),
              dataPoint.getClosePrice());
      volumeSeries.add(period, dataPoint.getVolume());
    }

    OHLCSeriesCollection candlestickDataset = new OHLCSeriesCollection();
    candlestickDataset.addSeries(ohlcSeries);

    NumberAxis priceAxis = new NumberAxis("Price");
    priceAxis.setAutoRangeIncludesZero(false);

    CandlestickRenderer candlestickRenderer = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE,
            false, new HighLowItemLabelGenerator(new SimpleDateFormat("dd-MM-yyyy"), new DecimalFormat("0.000")));

    candlestickRenderer.setUpPaint(new Color(80, 142, 84));
    candlestickRenderer.setDownPaint(new Color(181, 71, 71));
    candlestickRenderer.setSeriesPaint(0, Color.BLACK);

    XYPlot candlestickSubplot = new XYPlot(candlestickDataset, null, priceAxis, candlestickRenderer);
    candlestickSubplot.setBackgroundPaint(Color.white);

    /**
     * Create RSI subplot
     */

    DataProcessor dataProcessor = new DataProcessor();

    List<RSIValue> rsiValuesFull = dataProcessor.RSICalculator(thisStock.getStockDataPoints());
    List<RSIValue> rsiValues = StockDataParser.getStockDataInRange(rsiValuesFull, from.getTime(), to.getTime());

    TimeSeries rsiSeries = new TimeSeries("RSI");

    for (RSIValue rsiValue : rsiValues) {
      RegularTimePeriod period = new Day(new Date(rsiValue.getTimestamp()));
      rsiSeries.add(period, rsiValue.getRsi());
    }

    TimeSeriesCollection rsiDataset = new TimeSeriesCollection();
    rsiDataset.addSeries(rsiSeries);

    NumberAxis rsiAxis = new NumberAxis("RSI");
    rsiAxis.setAutoRangeIncludesZero(false);

    XYLineAndShapeRenderer rsiRenderer = new XYLineAndShapeRenderer(true, false);
    rsiRenderer.setSeriesPaint(0, Color.BLACK);

    XYPlot rsiSubplot = new XYPlot(rsiDataset, null, rsiAxis, rsiRenderer);
    rsiSubplot.setBackgroundPaint(Color.white);

    /**
     * Create volume subplot
     */

    TimeSeriesCollection volumeDataset = new TimeSeriesCollection();
    volumeDataset.addSeries(volumeSeries);

    NumberAxis volumeAxis = new NumberAxis("Volume");
    volumeAxis.setAutoRangeIncludesZero(false);
    volumeAxis.setNumberFormatOverride(new DecimalFormat("#,###"));

    XYBarRenderer.setDefaultBarPainter(new StandardXYBarPainter());

    XYBarRenderer timeRenderer = new XYBarRenderer(0.20);
    timeRenderer.setShadowVisible(false);
    timeRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Volume--> Date={1} Size={2}",
            new SimpleDateFormat("dd-MM-yyyy"), new DecimalFormat("0")));

    timeRenderer.setSeriesPaint(0, new Color(63, 98, 170));
    timeRenderer.setDrawBarOutline(true);
    timeRenderer.setDefaultOutlinePaint(Color.BLACK);

    XYPlot volumeSubplot = new XYPlot(volumeDataset, null, volumeAxis, timeRenderer);
    volumeSubplot.setBackgroundPaint(Color.white);

    DateAxis dateAxis = new DateAxis("Time");
    dateAxis.setDateFormatOverride(new SimpleDateFormat("kk:mm"));
    dateAxis.setLowerMargin(0.02);
    dateAxis.setUpperMargin(0.02);

    CombinedDomainXYPlot mainPlot = new CombinedDomainXYPlot(dateAxis);
    mainPlot.setGap(10.0);
    mainPlot.add(candlestickSubplot, 3);
    mainPlot.add(volumeSubplot, 1);
    mainPlot.add(rsiSubplot, 1); // Add RSI subplot

    Font segoeUIFont = new Font("Segoe UI", Font.PLAIN, 14);
    Font segoeUITitleFont = new Font("Segoe UI", Font.BOLD, 18);

    JFreeChart chart = new JFreeChart(
            thisStock.getName() + " from " + from + " to " + to,
            segoeUITitleFont,
            mainPlot,
            true
    );

    chart.removeLegend();

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
  private static List<Stock> getStockList (Index index) {
    if (index != null) {
      IndexWikipediaScraper scraper = new IndexWikipediaScraper();
      stockList = scraper.scraper(index.getWikipediaUrl());
    } else {
      return null;
    }
    return stockList;
  }
}