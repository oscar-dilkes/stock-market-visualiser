package org.smvisualiser;

import java.text.DecimalFormat;
import java.text.NumberFormat;

class MillionsNumberFormat extends NumberFormat {
  private final DecimalFormat df = new DecimalFormat("#.## 'M'");

  @Override
  public StringBuffer format(double number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
    return df.format(number / 1_000_000.0, toAppendTo, pos);
  }

  @Override
  public StringBuffer format(long number, StringBuffer toAppendTo, java.text.FieldPosition pos) {
    return df.format(number / 1_000_000.0, toAppendTo, pos);
  }

  @Override
  public Number parse(String source, java.text.ParsePosition parsePosition) {
    throw new UnsupportedOperationException("Parsing is not supported.");
  }
}
