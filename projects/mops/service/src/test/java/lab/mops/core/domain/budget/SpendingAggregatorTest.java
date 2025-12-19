package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Set;
import lab.mops.core.domain.time.Quarter;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class SpendingAggregatorTest extends TestBase {

  @Test
  void getSpendTotals_sumsGrandTotal() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2025, 1, 1), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.now(), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(3025, 1, 1), BigDecimal.valueOf(300)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.grandTotal()).isEqualTo(BigDecimal.valueOf(600));
  }

  @Test
  void getSpendTotals_sumsMonthlyTotalsOverMultipleYears() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2024, Month.DECEMBER, 1), BigDecimal.valueOf(-12)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 15), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 15), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.MARCH, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.MARCH, 15), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 15), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.MAY, 1), BigDecimal.valueOf(5)),
            Spend.of(LocalDate.of(2025, Month.MAY, 15), BigDecimal.valueOf(5)),
            Spend.of(LocalDate.of(2025, Month.JUNE, 1), BigDecimal.valueOf(6)),
            Spend.of(LocalDate.of(2025, Month.JUNE, 15), BigDecimal.valueOf(6)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(7)),
            Spend.of(LocalDate.of(2025, Month.JULY, 15), BigDecimal.valueOf(7)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 1), BigDecimal.valueOf(8)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 15), BigDecimal.valueOf(8)),
            Spend.of(LocalDate.of(2025, Month.SEPTEMBER, 1), BigDecimal.valueOf(9)),
            Spend.of(LocalDate.of(2025, Month.SEPTEMBER, 15), BigDecimal.valueOf(9)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 1), BigDecimal.valueOf(10)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 15), BigDecimal.valueOf(10)),
            Spend.of(LocalDate.of(2025, Month.NOVEMBER, 1), BigDecimal.valueOf(11)),
            Spend.of(LocalDate.of(2025, Month.NOVEMBER, 15), BigDecimal.valueOf(11)),
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 1), BigDecimal.valueOf(12)),
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 15), BigDecimal.valueOf(12)),
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(13)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.monthlyTotals())
        .extracting(
            LineItemSpendMonthlyTotal::month,
            LineItemSpendMonthlyTotal::year,
            LineItemSpendMonthlyTotal::total)
        .containsExactly(
            tuple(Month.DECEMBER, Year.of(2024), BigDecimal.valueOf(-12)),
            tuple(Month.JANUARY, Year.of(2025), BigDecimal.valueOf(2)),
            tuple(Month.FEBRUARY, Year.of(2025), BigDecimal.valueOf(4)),
            tuple(Month.MARCH, Year.of(2025), BigDecimal.valueOf(6)),
            tuple(Month.APRIL, Year.of(2025), BigDecimal.valueOf(8)),
            tuple(Month.MAY, Year.of(2025), BigDecimal.valueOf(10)),
            tuple(Month.JUNE, Year.of(2025), BigDecimal.valueOf(12)),
            tuple(Month.JULY, Year.of(2025), BigDecimal.valueOf(14)),
            tuple(Month.AUGUST, Year.of(2025), BigDecimal.valueOf(16)),
            tuple(Month.SEPTEMBER, Year.of(2025), BigDecimal.valueOf(18)),
            tuple(Month.OCTOBER, Year.of(2025), BigDecimal.valueOf(20)),
            tuple(Month.NOVEMBER, Year.of(2025), BigDecimal.valueOf(22)),
            tuple(Month.DECEMBER, Year.of(2025), BigDecimal.valueOf(24)),
            tuple(Month.JANUARY, Year.of(2026), BigDecimal.valueOf(13)));
  }

  @Test
  void getSpendTotals_sumsQuarterlyTotalsOverMultipleYears() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2024, Month.DECEMBER, 1), BigDecimal.valueOf(-4)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.MARCH, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.MAY, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.JUNE, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.SEPTEMBER, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.NOVEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(5)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.quarterlyTotals())
        .extracting(
            LineItemSpendQuarterlyTotal::quarter,
            LineItemSpendQuarterlyTotal::fiscalYear,
            LineItemSpendQuarterlyTotal::total)
        .containsExactly(
            tuple(Quarter.Q4, Year.of(2024), BigDecimal.valueOf(-4)),
            tuple(Quarter.Q1, Year.of(2025), BigDecimal.valueOf(3)),
            tuple(Quarter.Q2, Year.of(2025), BigDecimal.valueOf(6)),
            tuple(Quarter.Q3, Year.of(2025), BigDecimal.valueOf(9)),
            tuple(Quarter.Q4, Year.of(2025), BigDecimal.valueOf(12)),
            tuple(Quarter.Q1, Year.of(2026), BigDecimal.valueOf(5)));
  }

  @Test
  void getSpendTotals_sumsQuarterlyTotals_withShiftedFiscalYear() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.MARCH, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.MAY, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.JUNE, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.SEPTEMBER, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.NOVEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2026, Month.FEBRUARY, 1), BigDecimal.valueOf(1)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.FEBRUARY);

    assertThat(result.quarterlyTotals())
        .extracting(
            LineItemSpendQuarterlyTotal::quarter,
            LineItemSpendQuarterlyTotal::fiscalYear,
            LineItemSpendQuarterlyTotal::total)
        .containsExactly(
            tuple(Quarter.Q4, Year.of(2024), BigDecimal.valueOf(4)),
            tuple(Quarter.Q1, Year.of(2025), BigDecimal.valueOf(3)),
            tuple(Quarter.Q2, Year.of(2025), BigDecimal.valueOf(6)),
            tuple(Quarter.Q3, Year.of(2025), BigDecimal.valueOf(9)),
            tuple(Quarter.Q4, Year.of(2025), BigDecimal.valueOf(12)),
            tuple(Quarter.Q1, Year.of(2026), BigDecimal.valueOf(1)));
  }

  @Test
  void getSpendTotals_sumsAnnualTotals() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2024, Month.JANUARY, 1), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.of(2024, Month.JANUARY, 2), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 2), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(300)),
            Spend.of(LocalDate.of(2026, Month.JANUARY, 2), BigDecimal.valueOf(300)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.annualTotals())
        .extracting(LineItemSpendAnnualTotal::year, LineItemSpendAnnualTotal::total)
        .containsExactly(
            tuple(Year.of(2024), BigDecimal.valueOf(200)),
            tuple(Year.of(2025), BigDecimal.valueOf(400)),
            tuple(Year.of(2026), BigDecimal.valueOf(600)));
  }

  @Test
  void getSpendTotals_sortsMonthlyTotalsInIncreasingOrder() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(5)),
            Spend.of(LocalDate.of(2024, Month.DECEMBER, 1), BigDecimal.valueOf(-4)),
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.NOVEMBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 1), BigDecimal.valueOf(4)),
            Spend.of(LocalDate.of(2025, Month.MARCH, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.SEPTEMBER, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.MAY, 1), BigDecimal.valueOf(2)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(3)),
            Spend.of(LocalDate.of(2025, Month.JUNE, 1), BigDecimal.valueOf(2)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    var monthlyTotals = new ArrayList<>(result.monthlyTotals());
    assertThat(monthlyTotals)
        .extracting(LineItemSpendMonthlyTotal::year, LineItemSpendMonthlyTotal::month)
        .containsExactly(
            tuple(Year.of(2024), Month.DECEMBER),
            tuple(Year.of(2025), Month.JANUARY),
            tuple(Year.of(2025), Month.FEBRUARY),
            tuple(Year.of(2025), Month.MARCH),
            tuple(Year.of(2025), Month.APRIL),
            tuple(Year.of(2025), Month.MAY),
            tuple(Year.of(2025), Month.JUNE),
            tuple(Year.of(2025), Month.JULY),
            tuple(Year.of(2025), Month.AUGUST),
            tuple(Year.of(2025), Month.SEPTEMBER),
            tuple(Year.of(2025), Month.OCTOBER),
            tuple(Year.of(2025), Month.NOVEMBER),
            tuple(Year.of(2025), Month.DECEMBER),
            tuple(Year.of(2026), Month.JANUARY));
  }

  @Test
  void getSpendTotals_sortsQuarterTotalsInIncreasingOrder() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2024, Month.DECEMBER, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.OCTOBER, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(1)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    var quarterlyTotals = new ArrayList<>(result.quarterlyTotals());
    assertThat(quarterlyTotals)
        .extracting(LineItemSpendQuarterlyTotal::fiscalYear, LineItemSpendQuarterlyTotal::quarter)
        .containsExactly(
            tuple(Year.of(2024), Quarter.Q4),
            tuple(Year.of(2025), Quarter.Q1),
            tuple(Year.of(2025), Quarter.Q2),
            tuple(Year.of(2025), Quarter.Q3),
            tuple(Year.of(2025), Quarter.Q4),
            tuple(Year.of(2026), Quarter.Q1));
  }

  @Test
  void getSpendTotals_sortsAnnualTotalsInIncreasingOrder() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2026, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2024, Month.JANUARY, 1), BigDecimal.valueOf(1)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(1)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    var annualTotals = new ArrayList<>(result.annualTotals());
    assertThat(annualTotals)
        .extracting(LineItemSpendAnnualTotal::year)
        .containsExactly(Year.of(2024), Year.of(2025), Year.of(2026));
  }
}
