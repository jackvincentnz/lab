package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
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
  void getSpendTotals_sumsMonthlyTotals() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.of(2025, Month.JANUARY, 15), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(400)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.monthlyTotals())
        .extracting(LineItemSpendMonthlyTotal::month, LineItemSpendMonthlyTotal::total)
        .containsExactlyInAnyOrder(
            tuple(Month.JANUARY, BigDecimal.valueOf(300)),
            tuple(Month.FEBRUARY, BigDecimal.valueOf(400)));
  }

  @Test
  void getSpendTotals_sumsQuarterlyTotals() {
    var spendingSet =
        Set.of(
            Spend.of(LocalDate.of(2025, Month.JANUARY, 1), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.of(2025, Month.FEBRUARY, 1), BigDecimal.valueOf(100)),
            Spend.of(LocalDate.of(2025, Month.APRIL, 1), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(2025, Month.MAY, 1), BigDecimal.valueOf(200)),
            Spend.of(LocalDate.of(2025, Month.JULY, 1), BigDecimal.valueOf(300)),
            Spend.of(LocalDate.of(2025, Month.AUGUST, 1), BigDecimal.valueOf(300)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.JANUARY);

    assertThat(result.quarterlyTotals())
        .extracting(LineItemSpendQuarterlyTotal::quarter, LineItemSpendQuarterlyTotal::total)
        .containsExactlyInAnyOrder(
            tuple(Quarter.Q1, BigDecimal.valueOf(200)),
            tuple(Quarter.Q2, BigDecimal.valueOf(400)),
            tuple(Quarter.Q3, BigDecimal.valueOf(600)));
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
            Spend.of(LocalDate.of(2025, Month.DECEMBER, 1), BigDecimal.valueOf(4)));

    var result = SpendingAggregator.getSpendTotals(spendingSet, Month.FEBRUARY);

    assertThat(result.quarterlyTotals())
        .extracting(LineItemSpendQuarterlyTotal::quarter, LineItemSpendQuarterlyTotal::total)
        .containsExactlyInAnyOrder(
            tuple(Quarter.Q1, BigDecimal.valueOf(3)),
            tuple(Quarter.Q2, BigDecimal.valueOf(6)),
            tuple(Quarter.Q3, BigDecimal.valueOf(9)),
            tuple(Quarter.Q4, BigDecimal.valueOf(12)));
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
        .containsExactlyInAnyOrder(
            tuple(Year.of(2024), BigDecimal.valueOf(200)),
            tuple(Year.of(2025), BigDecimal.valueOf(400)),
            tuple(Year.of(2026), BigDecimal.valueOf(600)));
  }
}
