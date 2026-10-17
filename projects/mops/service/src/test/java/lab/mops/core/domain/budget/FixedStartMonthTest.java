package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import lab.mops.core.domain.time.Quarter;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class FixedStartMonthTest extends TestBase {

  @Test
  void fiscalYearOf_withJanuaryStart_returnsCalendarYear() {
    var calendar = new FixedStartMonth(Month.JANUARY);

    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 1, 1))).isEqualTo(Year.of(2025));
    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 12, 31))).isEqualTo(Year.of(2025));
  }

  @Test
  void quarterOf_withJanuaryStart_returnsCalendarQuarter() {
    var calendar = new FixedStartMonth(Month.JANUARY);

    assertThat(calendar.quarterOf(LocalDate.of(2025, 1, 1))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 3, 31))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 4, 1))).isEqualTo(Quarter.Q2);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 12, 31))).isEqualTo(Quarter.Q4);
  }

  @Test
  void fiscalYearOf_withAprilStart_startsYearOnFirstOfApril() {
    var calendar = new FixedStartMonth(Month.APRIL);

    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 3, 31))).isEqualTo(Year.of(2024));
    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 4, 1))).isEqualTo(Year.of(2025));
  }

  @Test
  void quarterOf_withAprilStart_startsQuartersFromApril() {
    var calendar = new FixedStartMonth(Month.APRIL);

    assertThat(calendar.quarterOf(LocalDate.of(2025, 3, 31))).isEqualTo(Quarter.Q4);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 4, 1))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 6, 30))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 7, 1))).isEqualTo(Quarter.Q2);
  }

  @Test
  void fiscalYearOf_withOctoberStart_keepsYearAcrossCalendarYearEnd() {
    var calendar = new FixedStartMonth(Month.OCTOBER);

    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 9, 30))).isEqualTo(Year.of(2024));
    assertThat(calendar.fiscalYearOf(LocalDate.of(2025, 10, 1))).isEqualTo(Year.of(2025));
    assertThat(calendar.fiscalYearOf(LocalDate.of(2026, 1, 1))).isEqualTo(Year.of(2025));
  }

  @Test
  void quarterOf_withOctoberStart_startsQuartersFromOctober() {
    var calendar = new FixedStartMonth(Month.OCTOBER);

    assertThat(calendar.quarterOf(LocalDate.of(2025, 9, 30))).isEqualTo(Quarter.Q4);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 10, 1))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2025, 12, 31))).isEqualTo(Quarter.Q1);
    assertThat(calendar.quarterOf(LocalDate.of(2026, 1, 1))).isEqualTo(Quarter.Q2);
  }
}
