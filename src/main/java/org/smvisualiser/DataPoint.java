package org.smvisualiser;

public abstract class DataPoint {
  private final long timestamp;

  protected DataPoint(long timestamp) {
    this.timestamp = timestamp;
  }

  public long getTimestamp() {
    return this.timestamp;
  }
}
