package org.smvisualiser;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.toedter.calendar.JDateChooser;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.HighLowItemLabelGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
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
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Timer;


public class UI {

  private static JLabel countLabel;
  private static LinkedList<Long> requestTimestamps = new LinkedList<>();
  private static List<Stock> stockList;
  private static PolygonClient client;

  private static AtomicReference<Boolean> isMa;

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

    Date currentDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.add(Calendar.YEAR, -2);

    Date twoYearsPriorDate = calendar.getTime();

    // Start Date Picker Panel
    JPanel startDatePanel = new JPanel();
    JLabel startDateLabel = new JLabel("Start Date");

    JDateChooser startDateChooser = new JDateChooser();
    startDateChooser.setMinSelectableDate(twoYearsPriorDate);
    startDateChooser.setMaxSelectableDate(currentDate);

    startDatePanel.add(startDateLabel);
    startDatePanel.add(startDateChooser);

    // End Date Picker Panel
    JPanel endDatePanel = new JPanel();
    JLabel endDateLabel = new JLabel("End Date");

    JDateChooser endDateChooser = new JDateChooser();
    endDateChooser.setMinSelectableDate(twoYearsPriorDate);
    endDateChooser.setMaxSelectableDate(currentDate);

    startDatePanel.add(endDateLabel);
    startDatePanel.add(endDateChooser);

    JCheckBox showMA = new JCheckBox("Show Simple Moving Average");

    JLabel periodLengthLabel = new JLabel("Period Length:");
    JTextField periodLengthField = new JTextField(20); // or JTextArea

    // Initially hide the textbox
    periodLengthLabel.setVisible(false);
    periodLengthField.setVisible(false);

    isMa = new AtomicReference<>(false);

    // Add action listener to checkbox
    showMA.addActionListener(e -> {
      // Toggle visibility of the textbox
      periodLengthLabel.setVisible(showMA.isSelected());
      periodLengthField.setVisible(showMA.isSelected());
      isMa.set(showMA.isSelected());
    });

    inputPanel.add(indexLabel, gbc);
    inputPanel.add(indexBox, gbc);
    inputPanel.add(symbolLabel, gbc);
    inputPanel.add(stockBox, gbc);
    inputPanel.add(startDatePanel, gbc);
    inputPanel.add(endDatePanel, gbc);
    inputPanel.add(showMA, gbc);
    inputPanel.add(periodLengthLabel, gbc);
    inputPanel.add(periodLengthField, gbc);
    inputPanel.add(submitButton, gbc);
    inputPanel.add(countLabel, gbc);

    mainPanel.add(inputPanel, BorderLayout.WEST);


    // Chart Panel
    JPanel chartPanel = new JPanel(new BorderLayout());
    chartPanel.setPreferredSize(new Dimension(800, 600)); // Set preferred size

    mainPanel.add(chartPanel, BorderLayout.CENTER);

    submitButton.addActionListener(e -> {
      if (requestTimestamps.size() >= 5) {
        JOptionPane.showMessageDialog(frame, "Request limit exceeded: Try again once counter goes below 5.");
        return;
      }
      long currentTime = System.currentTimeMillis();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

      String twoYearsPrior = sdf.format(twoYearsPriorDate);
      String today = sdf.format(currentDate);

      Stock thisStock = (Stock) stockBox.getSelectedItem();

      Date startDate = startDateChooser.getDate();
      Date endDate = endDateChooser.getDate();

      if (startDate == null || endDate == null) {
        JOptionPane.showMessageDialog(frame, "Please select both start and end dates.");
        return;  // Exit the ActionListener if dates are not selected
      }

      String from = sdf.format(startDate);
      String to = sdf.format(endDate);

        try {
          if (!thisStock.isRetrievalSuccess()) {
            client = new PolygonClient();
            assert thisStock != null;
            client.retrieveData(thisStock, 1, "day", twoYearsPrior, today);
            requestTimestamps.add(currentTime);
            updateCountLabel();
          }

          if (!thisStock.isRetrievalSuccess()) {
            JOptionPane.showMessageDialog(frame, "Data retrieval error: Ensure ticker symbol exists and date range is valid.");
            return;
          }

          sdf = new SimpleDateFormat("dd-MM-yyyy");

//          from = sdf.format(startDate);
//          to = sdf.format(endDate);

          boolean showMa = isMa.get();

          JFreeChart chart = createChart(thisStock, startDate, endDate, showMa, periodLengthField, frame);

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
    chartPanelComponent.setZoomOutFactor(1.0); // Set maximum zoom out factor to 1.0
    return chartPanelComponent;
  }


  public static JFreeChart createChart(Stock thisStock, Date from, Date to, Boolean isMa, JTextField periodLengthField, JFrame frame) {

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

    List<RSIValue> rsiValuesFull;

    if (!thisStock.isRsiCalculated()) {
      rsiValuesFull = dataProcessor.RSICalculator(thisStock.getStockDataPoints());
      thisStock.setRsiValues(rsiValuesFull);
    } else {
      rsiValuesFull = thisStock.getRsiValues();
    }

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
    volumeAxis.setNumberFormatOverride(new MillionsNumberFormat());

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

    /**
     * Create graph
     */

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


    /**
     * Create MA subplot
     */

    if (isMa) {
      // Retrieve SMA data using your smaCalculator method
      int periodLengthValue = Integer.parseInt(periodLengthField.getText().trim()); // Parse period length
      List<MAValue> movingAveragesFull = dataProcessor.SMACalculator(thisStock.getStockDataPoints(), periodLengthValue);
      List<MAValue> movingAverages = StockDataParser.getStockDataInRange(movingAveragesFull, from.getTime(), to.getTime());

      if (movingAverages.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Couldn't show moving averages for this date range.");
      }

      // Create a TimeSeries for SMA
      TimeSeries smaSeries = new TimeSeries("SMA(" + periodLengthValue + ")");
      for (MAValue mav : movingAverages) {
        RegularTimePeriod period = new Day(new Date(mav.getTimestamp()));
        smaSeries.add(period, mav.getMa());
      }

      // Create a dataset for SMA
      TimeSeriesCollection smaDataset = new TimeSeriesCollection();
      smaDataset.addSeries(smaSeries);

      // Create renderer for SMA (use XYLineAndShapeRenderer or similar)
      XYLineAndShapeRenderer smaRenderer = new XYLineAndShapeRenderer(true, false);
      smaRenderer.setSeriesPaint(0, Color.BLUE); // Set color for SMA line

      candlestickSubplot.setDataset(1, smaDataset);
      candlestickSubplot.setRenderer(1, smaRenderer);
    }

    JFreeChart chart = new JFreeChart(
            thisStock.getName() + " from " + sdf.format(from) + " to " + sdf.format(to),
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

    while (!requestTimestamps.isEmpty() && requestTimestamps.getFirst() < oneMinuteAgo) {
      requestTimestamps.removeFirst();
    }

    int count = requestTimestamps.size();

    countLabel.setText("Requests made in last minute: " + count);
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