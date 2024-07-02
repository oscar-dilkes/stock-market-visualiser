package org.smvisualiser;

public class RSIValue extends DataPoint {
  private final double rsi;

  public RSIValue(double rsi, long timestamp) {
    super(timestamp);
    this.rsi = rsi;
  }

  public double getRsi() {
    return this.rsi;
  }
}