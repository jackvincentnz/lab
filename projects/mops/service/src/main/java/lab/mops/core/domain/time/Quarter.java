package lab.mops.core.domain.time;

public enum Quarter {
  Q1,
  Q2,
  Q3,
  Q4;

  public static Quarter fromInt(int quarter) {
    if (quarter < 1 || quarter > 4) {
      throw new IllegalArgumentException("Quarter must be between 1 and 4");
    }
    return Quarter.values()[quarter - 1];
  }
}
