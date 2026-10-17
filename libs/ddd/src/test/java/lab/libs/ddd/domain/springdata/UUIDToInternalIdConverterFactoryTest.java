package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class UUIDToInternalIdConverterFactoryTest extends TestBase {

  UUIDToInternalIdConverterFactory factory = new UUIDToInternalIdConverterFactory();

  @Test
  void getConverter_targetType_convertsToTargetType() {
    var converter = factory.getConverter(TestId.class);
    var uuid = UUID.fromString(randomId());

    var result = converter.convert(uuid);

    assertThat(result).isEqualTo(new TestId(uuid));
  }

  static class TestId extends InternalId {
    TestId(UUID uuid) {
      super(uuid);
    }
  }
}
