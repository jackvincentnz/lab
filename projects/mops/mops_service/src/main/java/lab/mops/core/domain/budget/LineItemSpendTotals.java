package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

public record LineItemSpendTotals(
    Collection<LineItemSpendMonthlyTotal> monthlyTotals,
    Collection<LineItemSpendQuarterlyTotal> quarterlyTotals,
    Collection<LineItemSpendAnnualTotal> annualTotals,
    BigDecimal grandTotal) {

  public LineItemSpendTotals(
      Collection<LineItemSpendMonthlyTotal> monthlyTotals,
      Collection<LineItemSpendQuarterlyTotal> quarterlyTotals,
      Collection<LineItemSpendAnnualTotal> annualTotals,
      BigDecimal grandTotal) {
    this.monthlyTotals = monthlyTotals;
    this.quarterlyTotals = quarterlyTotals;
    this.annualTotals = annualTotals;
    this.grandTotal = grandTotal;
    Objects.requireNonNull(monthlyTotals, "monthlyTotals must not be null");
    Objects.requireNonNull(quarterlyTotals, "quarterlyTotals must not be null");
    Objects.requireNonNull(annualTotals, "annualTotals must not be null");
    Objects.requireNonNull(grandTotal, "grandTotal must not be null");
  }
}
