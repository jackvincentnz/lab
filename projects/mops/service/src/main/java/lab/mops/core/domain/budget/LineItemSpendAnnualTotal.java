package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;

public record LineItemSpendAnnualTotal(Year year, BigDecimal total) {

  public LineItemSpendAnnualTotal(Year year, BigDecimal total) {
    this.year = year;
    this.total = total;
    Objects.requireNonNull(year, "year must not be null");
    Objects.requireNonNull(total, "total must not be null");
  }
}
