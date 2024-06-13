package org.smvisualiser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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
        JOptionPane.showMessageDialog(frame, "Fetching data for symbol: " + symbol);
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