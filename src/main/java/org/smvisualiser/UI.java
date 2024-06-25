package org.smvisualiser;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.toedter.calendar.JDateChooser;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UI {

  public static LinkedList<Long> requestTimestamps = new LinkedList<>();
  public static AtomicReference<Boolean> isMa = new AtomicReference<>(false);
  private static JLabel countLabel;
  private static List<Stock> stockList;

  public static void display() {
    SwingUtilities.invokeLater(UI::createAndShowGUI);
  }

  public static void createAndShowGUI() {
    FlatIntelliJLaf.setup();
    JFrame frame = new JFrame("Stock Market Visualiser");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    JComboBox<Index> indexBox = new JComboBox<>();
    JComboBox<Stock> stockBox = new JComboBox<>();
    JDateChooser startDateChooser = new JDateChooser();
    JDateChooser endDateChooser = new JDateChooser();
    JCheckBox showMA = new JCheckBox("Show Simple Moving Average");
    JLabel periodLengthLabel = new JLabel("Period Length:");
    JTextField periodLengthField = new JTextField(20);
    JButton submitButton = new JButton("Submit");
    countLabel = new JLabel("Presses in last minute: 0");

    periodLengthLabel.setVisible(showMA.isSelected());
    periodLengthField.setVisible(showMA.isSelected());

    UIComponents uiComponents = new UIComponents();
    JPanel inputPanel = uiComponents.createInputPanel(frame, indexBox, stockBox, startDateChooser, endDateChooser, showMA, periodLengthLabel, periodLengthField, submitButton, countLabel);

    mainPanel.add(inputPanel, BorderLayout.WEST);

    JPanel chartPanel = new JPanel(new BorderLayout());
    chartPanel.setPreferredSize(new Dimension(800, 600));
    mainPanel.add(chartPanel, BorderLayout.CENTER);

    EventHandlers eventHandlers = new EventHandlers();

    stockList = eventHandlers.handleIndexBox(frame, indexBox, stockBox);
    for (Stock stock : stockList) {
      stockBox.addItem(stock);
    }

    indexBox.addActionListener(e -> {
      stockBox.removeAllItems();
      stockList = eventHandlers.handleIndexBox(frame, indexBox, stockBox);
    });

    isMa = new AtomicReference<>(false);

    // Add action listener to checkbox
    showMA.addActionListener(e -> {
      // Toggle visibility of the textbox
      periodLengthLabel.setVisible(showMA.isSelected());
      periodLengthField.setVisible(showMA.isSelected());
      isMa.set(showMA.isSelected());
    });

    submitButton.addActionListener(e -> {
      try {
        eventHandlers.handleSubmit(frame, stockBox, startDateChooser, endDateChooser, periodLengthField, chartPanel);
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
  public static ChartPanel getChartPanel(JFreeChart chart) {
    ChartPanel chartPanelComponent = new ChartPanel(chart);
    chartPanelComponent.setPreferredSize(new Dimension(800, 600));
    chartPanelComponent.setMouseZoomable(true);
    chartPanelComponent.setMouseWheelEnabled(true);
    chartPanelComponent.setDomainZoomable(true);
    chartPanelComponent.setRangeZoomable(true);
    chartPanelComponent.setPreferredSize(new Dimension(1680, 1100));
    chartPanelComponent.setZoomTriggerDistance(Integer.MAX_VALUE);
    chartPanelComponent.setFillZoomRectangle(true);
    chartPanelComponent.setZoomOutlinePaint(new Color(0f, 0f, 0f, 0f));
    chartPanelComponent.setPopupMenu(null);
    return chartPanelComponent;
  }

  public static void updateCountLabel() {
    long currentTime = System.currentTimeMillis();
    requestTimestamps.removeIf(timestamp -> currentTime - timestamp > 60000);
    countLabel.setText("Requests made in last minute: " + requestTimestamps.size());
  }
}
