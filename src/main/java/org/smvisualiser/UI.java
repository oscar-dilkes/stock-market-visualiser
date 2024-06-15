package org.smvisualiser;

import yahoofinance.*;
import yahoofinance.histquotes.HistoricalQuote;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public class UI {
  public static void display() {
    SwingUtilities.invokeLater(UI::createAndShowGUI);
  }

  public static void createAndShowGUI() {
    JFrame frame = new JFrame("HW");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10,10,10,10));

    JLabel instructionLabel = new JLabel("Enter Symbol:");
    panel.add(instructionLabel, BorderLayout.NORTH);

    JPanel inputPanel = new JPanel(new FlowLayout());

    JTextField symbolField = new JTextField(10);
    inputPanel.add(symbolField);

    JButton submitButton = new JButton("Submit");
    inputPanel.add(submitButton);

    panel.add(inputPanel, BorderLayout.CENTER);

    submitButton.addActionListener(e -> {
      String symbol = symbolField.getText().trim();
      if (!symbol.isEmpty()) {
        try {
          Stock thisStock = StockData.retrieveData(symbol);
          List<HistoricalQuote> history = thisStock.getHistory();


          DefaultListModel listModel = new DefaultListModel();
          for (HistoricalQuote quote : history) {
            listModel.addElement(quote.getDate() + ": High = " + quote.getHigh());
          }
          JList<String> list = new JList(listModel);

          panel.remove(1);

          instructionLabel.setText(symbol);

          panel.add(new JScrollPane(list), BorderLayout.CENTER);
          panel.revalidate();
          panel.repaint();
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      }
      else {
        JOptionPane.showMessageDialog(frame, "Please enter a symbol.");

      }
    });

    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }
}