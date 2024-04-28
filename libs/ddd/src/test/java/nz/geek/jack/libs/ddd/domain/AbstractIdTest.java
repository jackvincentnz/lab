package nz.geek.jack.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AbstractIdTest {

  @Test
  void new_createsUniqueId() {
    var id1 = new TestId();
    var id2 = new TestId();

    assertThat(id1.toString()).isNotEqualTo(id2.toString());
  }

  @Test
  void toString_returnsIdString() {
    var uuid = UUID.randomUUID();
    var id = new TestId(uuid);

    assertThat(id.toString()).isEqualTo(uuid.toString());
  }

  @Test
  void equals_sameInstance() {
    var id = new TestId();
    var copy = id;

    assertThat(id).isEqualTo(copy);
  }

  @Test
  void equals_otherInstance() {
    var uuid = UUID.randomUUID();
    var first = new TestId(uuid);
    var second = new TestId(uuid);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_doesNotEqualDifferentId() {
    var uuid1 = UUID.randomUUID();
    var uuid2 = UUID.randomUUID();
    var first = new TestId(uuid1);
    var second = new TestId(uuid2);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_doesNotEqualNullOther() {
    var id = new TestId();

    assertThat(id).isNotEqualTo(null);
  }

  @Test
  void equals_doesNotEqualOtherClass() {
    var id = UUID.randomUUID();
    var testId = new TestId(id);
    var anotherTestId = new AnotherTestId(id);

    assertThat(testId).isNotEqualTo(anotherTestId);
  }

  @Test
  void hashCode_isSameForMatchingInstances() {
    var uuid = UUID.randomUUID();
    var first = new TestId(uuid);
    var second = new TestId(uuid);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  static class TestId extends AbstractId {
    TestId() {
      super();
    }

    TestId(UUID id) {
      super(id);
    }
  }

  static class AnotherTestId extends AbstractId {
    AnotherTestId(UUID id) {
      super(id);
    }
  }
}
