package nz.geek.jack.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import org.junit.jupiter.api.Test;

class AbstractIdToUUIDConverterTest {

  @Test
  void convert_convertsToUUID() {
    var id = new TestId();
    var converter = new AbstractIdToUUIDConverter();

    var uuid = converter.convert(id);

    assertThat(uuid).isInstanceOf(UUID.class);
    assertThat(uuid).isEqualTo(id.toUUID());
  }

  static class TestId extends AbstractId {
    TestId() {
      super();
    }
  }
}
