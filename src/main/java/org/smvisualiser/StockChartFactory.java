package org.smvisualiser;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class StockChartFactory {

  public JFreeChart createChart(Stock stock, Date from, Date to, boolean isMa, int periodLength, JFrame frame) {
    OHLCSeriesCollection candlestickDataset = createCandlestickDataset(stock, frame, from, to);
    TimeSeriesCollection volumeDataset = createVolumeDataset(stock, from, to);
    TimeSeriesCollection rsiDataset = createRSIDataset(stock, from, to);

    return createCombinedChart(candlestickDataset, volumeDataset, rsiDataset, isMa, periodLength, stock, from, to);
  }

  private OHLCSeriesCollection createCandlestickDataset(Stock stock,JFrame frame, Date from, Date to) {
    OHLCSeries ohlcSeries = new OHLCSeries(stock.getTicker());

    if (stock.isRetrievalSuccess()) {
      for (StockDataPoint dataPoint : StockDataParser.getStockDataInRange(stock.getStockDataPoints(), from.getTime(), to.getTime())) {
        RegularTimePeriod period = new Day(new Date(dataPoint.getTimestamp()));
        ohlcSeries.add(period, dataPoint.getOpenPrice(), dataPoint.getHighPrice(), dataPoint.getLowPrice(), dataPoint.getClosePrice());
      }
    } else {
      JOptionPane.showMessageDialog(frame, "Data retrieval error: Ensure ticker symbol exists and date range is valid.");
    }

    OHLCSeriesCollection dataset = new OHLCSeriesCollection();
    dataset.addSeries(ohlcSeries);

    return dataset;
  }

  private TimeSeriesCollection createVolumeDataset(Stock stock, Date from, Date to) {
    TimeSeries volumeSeries = new TimeSeries(stock.getTicker());

    for (StockDataPoint dataPoint : StockDataParser.getStockDataInRange(stock.getStockDataPoints(), from.getTime(), to.getTime())) {
      RegularTimePeriod period = new Day(new Date(dataPoint.getTimestamp()));
      volumeSeries.add(period, dataPoint.getVolume());
    }

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(volumeSeries);

    return dataset;
  }

  private TimeSeriesCollection createRSIDataset(Stock stock, Date from, Date to) {
    List<RSIValue> rsiValuesFull;

    if (!stock.isRsiCalculated()) {
      rsiValuesFull = DataProcessor.RSICalculator(stock.getStockDataPoints());
      stock.setRsiValues(rsiValuesFull);
    } else {
      rsiValuesFull = stock.getRsiValues();
    }

    List<RSIValue> rsiValues = StockDataParser.getStockDataInRange(rsiValuesFull, from.getTime(), to.getTime());

    TimeSeries rsiSeries = new TimeSeries("RSI");

    for (RSIValue rsiValue : rsiValues) {
      RegularTimePeriod period = new Day(new Date(rsiValue.getTimestamp()));
      rsiSeries.add(period, rsiValue.getRsi());
    }

    TimeSeriesCollection rsiDataset = new TimeSeriesCollection();
    rsiDataset.addSeries(rsiSeries);

    return rsiDataset;
  }

  private JFreeChart createCombinedChart(OHLCSeriesCollection candlestickDataset, TimeSeriesCollection volumeDataset, TimeSeriesCollection rsiDataset, boolean isMa, int periodLength, Stock stock, Date from, Date to) {
    DateAxis dateAxis = new DateAxis("Time");
    dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy"));
    dateAxis.setLowerMargin(0.02);
    dateAxis.setUpperMargin(0.02);

    XYPlot candlestickPlot = createCandlestickPlot(candlestickDataset);
    XYPlot volumePlot = createVolumePlot(volumeDataset);
    XYPlot rsiPlot = createRSIPlot(rsiDataset);

    CombinedDomainXYPlot combinedPlot = new CombinedDomainXYPlot(dateAxis);
    combinedPlot.setGap(10.0);
    combinedPlot.add(candlestickPlot, 3);
    combinedPlot.add(volumePlot, 1);
    combinedPlot.add(rsiPlot, 1);

    if (isMa) {
      TimeSeriesCollection maDataset = createMADataset(stock, from, to, periodLength);
      addMAToPlot(candlestickPlot, maDataset);
    }

    JFreeChart chart = new JFreeChart(
            stock.getName() + " from " + new SimpleDateFormat("dd-MM-yyyy").format(from) + " to " + new SimpleDateFormat("dd-MM-yyyy").format(to),
            new Font("Segoe UI", Font.BOLD, 18),
            combinedPlot,
            true
    );

    chart.removeLegend();

    return chart;
  }

  private XYPlot createCandlestickPlot(OHLCSeriesCollection dataset) {
    NumberAxis priceAxis = new NumberAxis("Price");
    priceAxis.setAutoRangeIncludesZero(false);

    CandlestickRenderer renderer = new CandlestickRenderer();
    renderer.setUpPaint(new Color(80, 142, 84));
    renderer.setDownPaint(new Color(181, 71, 71));
    renderer.setSeriesPaint(0, Color.BLACK);
    renderer.setDefaultOutlineStroke(new BasicStroke(0.004f));

    XYPlot plot = new XYPlot(dataset, null, priceAxis, renderer);
    plot.setBackgroundPaint(Color.white);

    return plot;
  }

  private XYPlot createVolumePlot(TimeSeriesCollection dataset) {
    NumberAxis volumeAxis = new NumberAxis("Volume");
    volumeAxis.setAutoRangeIncludesZero(false);
    volumeAxis.setNumberFormatOverride(new MillionsNumberFormat());

    XYBarRenderer renderer = new XYBarRenderer(0.20);
    renderer.setShadowVisible(false);
    renderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator("Volume: Date={1} Size={2}", new SimpleDateFormat("dd-MM-yyyy"), new DecimalFormat("0")));
    renderer.setSeriesPaint(0, new Color(63, 98, 170));
    renderer.setDrawBarOutline(true);
    renderer.setDefaultOutlinePaint(Color.BLACK);

    XYPlot plot = new XYPlot(dataset, null, volumeAxis, renderer);
    plot.setBackgroundPaint(Color.white);

    return plot;
  }

  private XYPlot createRSIPlot(TimeSeriesCollection dataset) {
    NumberAxis rsiAxis = new NumberAxis("RSI");
    rsiAxis.setAutoRangeIncludesZero(false);

    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
    renderer.setSeriesPaint(0, Color.BLACK);

    XYPlot plot = new XYPlot(dataset, null, rsiAxis, renderer);
    plot.setBackgroundPaint(Color.white);

    // Create and add the ValueMarkers for y = 70 and y = 30
    addHorizontalLine(plot, 70, Color.RED, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5.0f}, 0.0f));
    addHorizontalLine(plot, 30, Color.BLUE, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{5.0f}, 0.0f));


    return plot;
  }

  private void addHorizontalLine(XYPlot plot, double value, Color color, Stroke stroke) {
    ValueMarker marker = new ValueMarker(value);
    marker.setPaint(color);
    marker.setStroke(stroke);
    plot.addRangeMarker(marker);
  }

  private TimeSeriesCollection createMADataset(Stock stock, Date from, Date to, int periodLength) {
    TimeSeries maSeries = new TimeSeries("SMA(" + periodLength + ")");
    List<MAValue> maValues = new DataProcessor().SMACalculator(stock.getStockDataPoints(), periodLength);

    for (MAValue maValue : StockDataParser.getStockDataInRange(maValues, from.getTime(), to.getTime())) {
      RegularTimePeriod period = new Day(new Date(maValue.getTimestamp()));
      maSeries.add(period, maValue.getMa());
    }

    TimeSeriesCollection dataset = new TimeSeriesCollection();
    dataset.addSeries(maSeries);

    return dataset;
  }

  private void addMAToPlot(XYPlot plot, TimeSeriesCollection maDataset) {
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
    renderer.setSeriesPaint(0, Color.BLUE);
    plot.setDataset(1, maDataset);
    plot.setRenderer(1, renderer);
  }
}
