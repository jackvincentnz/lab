package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class StringToExternalIdConverterFactoryTest extends TestBase {

  StringToExternalIdConverterFactory factory = new StringToExternalIdConverterFactory();

  @Test
  void getConverter_targetType_convertsToTargetType() {
    var converter = factory.getConverter(TestExternalId.class);
    var id = randomString();

    var result = converter.convert(id);

    assertThat(result).isEqualTo(new TestExternalId(id));
  }
}
