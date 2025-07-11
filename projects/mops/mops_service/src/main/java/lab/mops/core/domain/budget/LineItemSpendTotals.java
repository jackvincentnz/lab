package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.util.Collection;

public record LineItemSpendTotals(
    BigDecimal grandTotal,
    Collection<LineItemSpendAnnualTotal> annualTotals,
    Collection<LineItemSpendQuarterlyTotal> quarterlyTotals,
    Collection<LineItemSpendMonthlyTotal> monthlyTotals) {}
