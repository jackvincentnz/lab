package nz.geek.jack.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class StringValueToStringConverterTest extends TestBase {

  StringValueToStringConverter converter = new StringValueToStringConverter();

  @Test
  void convert() {
    var string = randomString();

    var result = converter.convert(new TestValue(string));

    assertThat(result).isEqualTo(string);
  }
}
