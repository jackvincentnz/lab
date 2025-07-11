package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Objects;
import lab.mops.core.domain.time.Quarter;

public record LineItemSpendQuarterlyTotal(Quarter quarter, Year fiscalYear, BigDecimal total) {

  public LineItemSpendQuarterlyTotal(Quarter quarter, Year fiscalYear, BigDecimal total) {
    this.quarter = quarter;
    this.fiscalYear = fiscalYear;
    this.total = total;
    Objects.requireNonNull(quarter, "quarter must not be null");
    Objects.requireNonNull(fiscalYear, "fiscalYear must not be null");
    Objects.requireNonNull(total, "total must not be null");
  }
}
