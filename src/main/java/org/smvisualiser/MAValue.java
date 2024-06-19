package org.smvisualiser;

public class MAValue extends DataPoint {
  private final double ma;

  public MAValue(double ma, long timestamp) {
    super(timestamp);
    this.ma = ma;
  }

  public double getMa() {
    return this.ma;
  }
}

