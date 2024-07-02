package org.smvisualiser;

import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

public class UIComponents {
  public JPanel createInputPanel(JFrame frame, JComboBox<Index> indexBox, JComboBox<Stock> stockBox, JDateChooser startDateChooser, JDateChooser endDateChooser,
                                 JCheckBox showMA, JLabel periodLengthLabel, JTextField periodLengthField, JButton submitButton, JLabel countLabel) {
    JPanel inputPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx = 0;
    gbc.gridy = GridBagConstraints.RELATIVE;
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;

    inputPanel.add(new JLabel("Index:"), gbc);
    inputPanel.add(indexBoxSetup(indexBox), gbc);
    inputPanel.add(new JLabel("Ticker Symbol:"), gbc);
    inputPanel.add(stockBox, gbc);
    inputPanel.add(createDatePanel("Start Date", dateChooserSetup(startDateChooser)), gbc);
    inputPanel.add(createDatePanel("End Date", dateChooserSetup(endDateChooser)), gbc);
    inputPanel.add(showMA, gbc);
    inputPanel.add(periodLengthLabel, gbc);
    inputPanel.add(periodLengthField, gbc);
    inputPanel.add(submitButton, gbc);
    inputPanel.add(countLabel, gbc);

    return inputPanel;
  }

  private JDateChooser dateChooserSetup (JDateChooser dateChooser) {
    Date currentDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(currentDate);
    calendar.add(Calendar.YEAR, -2);

    Date twoYearsPriorDate = calendar.getTime();

    dateChooser.setMinSelectableDate(twoYearsPriorDate);
    dateChooser.setMaxSelectableDate(currentDate);

    return dateChooser;
  }

  private JComboBox<Index> indexBoxSetup (JComboBox<Index> indexBox) {
    indexBox.addItem(new Index("S&P 500", "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies"));
    indexBox.addItem(new Index("Nasdaq-100", "https://en.wikipedia.org/wiki/Nasdaq-100"));
    indexBox.addItem(new Index("FTSE 100", "https://en.wikipedia.org/wiki/FTSE_100_Index"));
    return indexBox;
  }

  private JPanel createDatePanel(String labelText, JDateChooser dateChooser) {
    JPanel datePanel = new JPanel();
    datePanel.add(new JLabel(labelText));
    datePanel.add(dateChooser);
    return datePanel;
  }
}
