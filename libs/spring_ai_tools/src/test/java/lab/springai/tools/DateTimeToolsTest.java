package lab.springai.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;

class DateTimeToolsTest {

  @Test
  void getCurrentDateTime_returnsDateTimeInUserTimezone() {
    var userTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    LocaleContextHolder.setTimeZone(userTimeZone);

    var now = Instant.now();
    var fixedClock = Clock.fixed(now, ZoneId.systemDefault());

    var dateTimeToolsWithFixedClock = new DateTimeTools(fixedClock);

    var result = dateTimeToolsWithFixedClock.getCurrentDateTime();

    assertThat(result).isEqualTo(now.atZone(userTimeZone.toZoneId()).toString());

    LocaleContextHolder.resetLocaleContext();
  }
}
