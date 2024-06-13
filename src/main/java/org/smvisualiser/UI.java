package org.smvisualiser;

import kong.unirest.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
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
          JSONObject stockData = thisStock.getData();
          JSONObject timeSeries = stockData.getJSONObject("Time Series (Daily)");
          Set<String> dates = timeSeries.keySet();
          DefaultListModel listModel = new DefaultListModel();
          for (String date : dates) {
            listModel.addElement(date + ": High = " + timeSeries.getJSONObject(date).get("2. high"));
          }
          JList list = new JList(listModel);
          panel.add(list, BorderLayout.SOUTH);
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