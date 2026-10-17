package lab.mops.core.domain.budget;

import java.math.BigDecimal;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import lab.mops.core.domain.time.Quarter;

class SpendingAggregator {

  private SpendingAggregator() {}

  static LineItemSpendTotals getSpendTotals(
      Collection<Spend> spending, FiscalCalendar fiscalCalendar) {
    Map<MonthYearKey, BigDecimal> monthlyMap = new HashMap<>();
    Map<QuarterFiscalYearKey, BigDecimal> quarterlyMap = new HashMap<>();
    Map<Year, BigDecimal> annualMap = new HashMap<>();
    var grandTotal = BigDecimal.ZERO;

    for (Spend spend : spending) {
      var amount = spend.getAmount();
      var spendDay = spend.getSpendDay();

      var fiscalYear = fiscalCalendar.fiscalYearOf(spendDay);

      var monthYearKey = new MonthYearKey(spendDay.getMonth(), Year.of(spendDay.getYear()));
      var quarterFiscalYearKey =
          new QuarterFiscalYearKey(fiscalCalendar.quarterOf(spendDay), fiscalYear);

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
            .toList();

    var quarterlyTotals =
        quarterlyMap.entrySet().stream()
            .map(
                e ->
                    new LineItemSpendQuarterlyTotal(
                        e.getKey().quarter(), e.getKey().fiscalYear(), e.getValue()))
            .sorted(
                Comparator.comparing(LineItemSpendQuarterlyTotal::fiscalYear)
                    .thenComparing(LineItemSpendQuarterlyTotal::quarter))
            .toList();

    var annualTotals =
        annualMap.entrySet().stream()
            .map(e -> new LineItemSpendAnnualTotal(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(LineItemSpendAnnualTotal::year))
            .toList();

    return new LineItemSpendTotals(monthlyTotals, quarterlyTotals, annualTotals, grandTotal);
  }

  private record MonthYearKey(Month month, Year year) {}

  private record QuarterFiscalYearKey(Quarter quarter, Year fiscalYear) {}
}
