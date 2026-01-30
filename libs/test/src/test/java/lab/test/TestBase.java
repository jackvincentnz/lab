package lab.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

public abstract class TestBase {

  protected String randomString() {
    return RandomStringUtils.randomAlphanumeric(16);
  }

  protected String randomString(int count) {
    return RandomStringUtils.randomAlphanumeric(count);
  }

  protected int randomInt() {
    return RandomUtils.nextInt();
  }

  protected String randomId() {
    return UUID.randomUUID().toString();
  }

  protected Clock fixedClock() {
    return fixedClock(Instant.now());
  }

  protected Clock fixedClock(Instant instant) {
    return Clock.fixed(instant, ZoneId.systemDefault());
  }
}
