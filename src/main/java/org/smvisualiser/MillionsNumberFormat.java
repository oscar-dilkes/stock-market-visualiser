package org.smvisualiser;

import java.text.DecimalFormat;

class MillionsNumberFormat extends DecimalFormat {
  public MillionsNumberFormat() {
    super("###,###,###.##M");
  }
}
