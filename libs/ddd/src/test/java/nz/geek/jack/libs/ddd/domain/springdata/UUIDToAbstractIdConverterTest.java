package nz.geek.jack.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import org.junit.jupiter.api.Test;

class UUIDToAbstractIdConverterTest {

  @Test
  void convert_convertsToTestId() {
    var converter = UUIDToAbstractIdConverter.of(TestId.class);
    var uuid = UUID.randomUUID();

    var testId = converter.convert(uuid);

    assertThat(testId).isInstanceOf(TestId.class);
    assertThat(testId).isEqualTo(new TestId(uuid));
  }

  static class TestId extends AbstractId {
    TestId(UUID uuid) {
      super(uuid);
    }
  }

  @Test
  void convert_rethrowsExceptions() {
    var converter = UUIDToAbstractIdConverter.of(ThrowingTestId.class);
    var uuid = UUID.randomUUID();

    assertThrows(RuntimeException.class, () -> converter.convert(uuid));
  }

  static class ThrowingTestId extends AbstractId {
    ThrowingTestId(UUID uuid) {
      throw new RuntimeException();
    }
  }
}
