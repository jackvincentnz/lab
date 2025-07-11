package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lab.mops.core.domain.time.Quarter;

class SpendingAggregator {

  private SpendingAggregator() {}

  static LineItemSpendTotals getSpendTotals(
      Collection<Spend> spending, Month fiscalYearStartMonthNumber) {
    var fiscalYearStartMonth = fiscalYearStartMonthNumber.getValue();
    var total = BigDecimal.ZERO;
    Map<Month, BigDecimal> monthlyMap = new HashMap<>();
    Map<Integer, BigDecimal> quarterlyMap = new HashMap<>();
    Map<Integer, BigDecimal> annualMap = new HashMap<>();

    for (Spend spend : spending) {
      var amount = spend.getAmount();
      var spendDay = spend.getSpendDay();

      // Update total
      total = total.add(amount);

      // Update monthly totals
      var month = spendDay.getMonth();
      monthlyMap.merge(month, amount, BigDecimal::add);

      // Update quarterly totals
      var quarter = (spendDay.getMonthValue() + (12 - fiscalYearStartMonth)) % 12 / 3 + 1;
      quarterlyMap.merge(quarter, amount, BigDecimal::add);

      // Update annual totals
      var fiscalYear =
          spendDay.getMonthValue() < fiscalYearStartMonth
              ? spendDay.getYear() - 1
              : spendDay.getYear();
      annualMap.merge(fiscalYear, amount, BigDecimal::add);
    }

    var monthlyTotals =
        monthlyMap.entrySet().stream()
            .map(e -> new LineItemSpendMonthlyTotal(e.getKey(), e.getValue()))
            .collect(Collectors.toList());

    var quarterlyTotals =
        quarterlyMap.entrySet().stream()
            .map(e -> new LineItemSpendQuarterlyTotal(Quarter.fromInt(e.getKey()), e.getValue()))
            .collect(Collectors.toList());

    var annualTotals =
        annualMap.entrySet().stream()
            .map(e -> new LineItemSpendAnnualTotal(Year.of(e.getKey()), e.getValue()))
            .collect(Collectors.toList());

    return new LineItemSpendTotals(total, annualTotals, quarterlyTotals, monthlyTotals);
  }
}
