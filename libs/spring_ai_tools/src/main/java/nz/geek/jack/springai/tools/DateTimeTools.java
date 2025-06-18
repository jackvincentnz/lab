package nz.geek.jack.springai.tools;

import java.time.Clock;
import java.time.Instant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

public class DateTimeTools {

  private final Clock clock;

  public DateTimeTools() {
    this(Clock.systemDefaultZone());
  }

  public DateTimeTools(Clock clock) {
    this.clock = clock;
  }

  @Tool(description = "Get the current date and time in the user's timezone")
  String getCurrentDateTime() {
    return Instant.now(clock).atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
  }
}
