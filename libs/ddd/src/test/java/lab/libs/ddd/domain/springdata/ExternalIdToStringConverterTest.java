package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ExternalIdToStringConverterTest extends TestBase {

  ExternalIdToStringConverter converter = new ExternalIdToStringConverter();

  @Test
  void convert_externalId_returnsId() {
    var id = randomString();

    var result = converter.convert(new TestExternalId(id));

    assertThat(result).isEqualTo(id);
  }
}
