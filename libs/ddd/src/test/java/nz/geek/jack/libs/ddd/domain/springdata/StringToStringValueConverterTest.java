package nz.geek.jack.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class StringToStringValueConverterTest extends TestBase {

  @Test
  void convert() {
    var converter = StringToStringValueConverter.of(TestValue.class);
    var string = randomString();

    var result = converter.convert(string);

    assertThat(result).isEqualTo(new TestValue(string));
  }
}
