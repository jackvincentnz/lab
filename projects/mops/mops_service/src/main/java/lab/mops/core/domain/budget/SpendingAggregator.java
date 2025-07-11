package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lab.mops.core.domain.time.Quarter;

class SpendingAggregator {

  private SpendingAggregator() {}

  static LineItemSpendTotals getSpendTotals(
      Collection<Spend> spending, Month fiscalYearStartMonth) {
    var fyStartMonthInt = fiscalYearStartMonth.getValue();

    Map<MonthYearKey, BigDecimal> monthlyMap = new HashMap<>();
    Map<QuarterFiscalYearKey, BigDecimal> quarterlyMap = new HashMap<>();
    Map<Integer, BigDecimal> annualMap = new HashMap<>();
    var grandTotal = BigDecimal.ZERO;

    for (Spend spend : spending) {
      var amount = spend.getAmount();
      var spendDay = spend.getSpendDay();

      var quarter = (spendDay.getMonthValue() + (12 - fyStartMonthInt)) % 12 / 3 + 1;
      var fiscalYear =
          spendDay.getMonthValue() < fyStartMonthInt ? spendDay.getYear() - 1 : spendDay.getYear();

      var monthYearKey = new MonthYearKey(spendDay.getMonth(), Year.of(spendDay.getYear()));
      var quarterFiscalYearKey =
          new QuarterFiscalYearKey(Quarter.fromInt(quarter), Year.of(fiscalYear));

      monthlyMap.merge(monthYearKey, amount, BigDecimal::add);
      quarterlyMap.merge(quarterFiscalYearKey, amount, BigDecimal::add);
      annualMap.merge(fiscalYear, amount, BigDecimal::add);
      grandTotal = grandTotal.add(amount);
    }

    var monthlyTotals =
        monthlyMap.entrySet().stream()
            .map(
                e ->
                    new LineItemSpendMonthlyTotal(
                        e.getKey().month(), e.getKey().year(), e.getValue()))
            .sorted(
                Comparator.comparing(LineItemSpendMonthlyTotal::year)
                    .thenComparing(LineItemSpendMonthlyTotal::month))
            .collect(Collectors.toList());

    var quarterlyTotals =
        quarterlyMap.entrySet().stream()
            .map(
                e ->
                    new LineItemSpendQuarterlyTotal(
                        e.getKey().quarter(), e.getKey().fiscalYear(), e.getValue()))
            .sorted(
                Comparator.comparing(LineItemSpendQuarterlyTotal::fiscalYear)
                    .thenComparing(LineItemSpendQuarterlyTotal::quarter))
            .collect(Collectors.toList());

    var annualTotals =
        annualMap.entrySet().stream()
            .map(e -> new LineItemSpendAnnualTotal(Year.of(e.getKey()), e.getValue()))
            .sorted(Comparator.comparing(LineItemSpendAnnualTotal::year))
            .collect(Collectors.toList());

    return new LineItemSpendTotals(monthlyTotals, quarterlyTotals, annualTotals, grandTotal);
  }

  private record MonthYearKey(Month month, Year year) {}

  private record QuarterFiscalYearKey(Quarter quarter, Year fiscalYear) {}
}
