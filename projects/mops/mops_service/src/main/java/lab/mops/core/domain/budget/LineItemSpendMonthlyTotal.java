package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Objects;

public record LineItemSpendMonthlyTotal(Month month, Year year, BigDecimal total) {

  public LineItemSpendMonthlyTotal(Month month, Year year, BigDecimal total) {
    this.month = month;
    this.year = year;
    this.total = total;
    Objects.requireNonNull(month, "month must not be null");
    Objects.requireNonNull(year, "year must not be null");
    Objects.requireNonNull(total, "total must not be null");
  }
}
