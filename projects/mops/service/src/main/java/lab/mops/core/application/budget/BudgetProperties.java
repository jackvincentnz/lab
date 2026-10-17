package lab.mops.core.application.budget;

import java.time.Month;
import lab.mops.core.domain.budget.FiscalCalendar;
import lab.mops.core.domain.budget.FixedStartMonth;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * How Mops groups budget spend into reporting periods.
 *
 * @param fiscalYearStartMonth the month that begins each fiscal year and its first quarter. January
 *     by default, so quarters and fiscal years match the calendar.
 */
@ConfigurationProperties("mops.budget")
public record BudgetProperties(@DefaultValue("JANUARY") Month fiscalYearStartMonth) {

  /** The calendar every budget uses unless something more specific applies to it. */
  public FiscalCalendar fiscalCalendar() {
    return new FixedStartMonth(fiscalYearStartMonth);
  }
}
