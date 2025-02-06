package nz.geek.jack.test;

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
}
