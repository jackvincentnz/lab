package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;
import org.junit.jupiter.api.Test;

class UUIDToInternalIdConverterTest {

  @Test
  void convert_convertsToTestId() {
    var converter = UUIDToInternalIdConverter.of(TestId.class);
    var uuid = UUID.randomUUID();

    var testId = converter.convert(uuid);

    assertThat(testId).isInstanceOf(TestId.class);
    assertThat(testId).isEqualTo(new TestId(uuid));
  }

  static class TestId extends InternalId {
    TestId(UUID uuid) {
      super(uuid);
    }
  }

  @Test
  void convert_rethrowsExceptions() {
    var converter = UUIDToInternalIdConverter.of(ThrowingTestId.class);
    var uuid = UUID.randomUUID();

    assertThrows(RuntimeException.class, () -> converter.convert(uuid));
  }

  static class ThrowingTestId extends InternalId {
    ThrowingTestId(UUID uuid) {
      throw new RuntimeException();
    }
  }
}
