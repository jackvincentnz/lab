package lab.mops.core.domain.budget;

import java.time.LocalDate;
import java.time.Year;
import lab.mops.core.domain.time.Quarter;

/**
 * Places a day in a fiscal year and quarter.
 *
 * <p>This is an interface rather than a start month because a start month cannot describe calendars
 * such as 4-4-5 or 52-53-week retail years, where periods do not begin on the first of a month.
 */
public interface FiscalCalendar {

  /** The fiscal year that contains the day, named by the calendar year it starts in. */
  Year fiscalYearOf(LocalDate day);

  /** The quarter of its fiscal year that contains the day. */
  Quarter quarterOf(LocalDate day);
}
