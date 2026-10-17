package lab.mops.core.domain.budget;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import lab.mops.core.domain.time.Quarter;

/**
 * A fiscal calendar whose year starts on the first day of the same month every year, with quarters
 * of three whole months.
 *
 * @param startMonth the month that begins each fiscal year and its first quarter.
 */
public record FixedStartMonth(Month startMonth) implements FiscalCalendar {

  @Override
  public Year fiscalYearOf(LocalDate day) {
    return day.getMonthValue() < startMonth.getValue()
        ? Year.of(day.getYear() - 1)
        : Year.of(day.getYear());
  }

  @Override
  public Quarter quarterOf(LocalDate day) {
    var monthsIntoFiscalYear = (day.getMonthValue() + 12 - startMonth.getValue()) % 12;
    return Quarter.fromInt(monthsIntoFiscalYear / 3 + 1);
  }
}
