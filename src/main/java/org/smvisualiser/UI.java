package org.smvisualiser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

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

    JTextField tickerField = new JTextField(10);
    inputPanel.add(tickerField);

    JButton submitButton = new JButton("Submit");
    inputPanel.add(submitButton);

    panel.add(inputPanel, BorderLayout.CENTER);

    submitButton.addActionListener(e -> {
      String ticker = tickerField.getText().trim();
      if (!ticker.isEmpty()) {
        try {
          PolygonClient client = new PolygonClient();
          Stock thisStock = client.retrieveData(ticker, 1, "day", "2024-04-14", "2024-06-14");

          System.out.println(thisStock.getData());

          DefaultListModel listModel = new DefaultListModel();
//          for (HistoricalQuote quote : history) {
//            listModel.addElement(quote.getDate() + ": High = " + quote.getHigh());
//          }
          JList<String> list = new JList(listModel);

          panel.remove(1);

          instructionLabel.setText(ticker);

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