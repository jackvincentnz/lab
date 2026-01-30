package lab.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ExternalIdTest extends TestBase {

  @Test
  void new_throwsWhenNull() {
    assertThatThrownBy(() -> new TestId(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void new_throwsWhenBlank() {
    assertThatThrownBy(() -> new TestId(" ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void toString_returnsIdString() {
    var idString = randomId();
    var id = new TestId(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }

  @Test
  void equals_sameInstance() {
    var id = new TestId(randomId());
    var copy = id;

    assertThat(id).isEqualTo(copy);
  }

  @Test
  void equals_otherInstance() {
    var idString = randomId();
    var first = new TestId(idString);
    var second = new TestId(idString);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_doesNotEqualDifferentId() {
    var idString1 = randomId();
    var idString2 = randomId();
    var first = new TestId(idString1);
    var second = new TestId(idString2);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_doesNotEqualNullOther() {
    var id = new TestId(randomId());

    assertThat(id).isNotEqualTo(null);
  }

  @Test
  void equals_doesNotEqualOtherClass() {
    var idString = randomId();
    var testId = new TestId(idString);
    var anotherTestId = new AnotherTestId(idString);

    assertThat(testId).isNotEqualTo(anotherTestId);
  }

  @Test
  void hashCode_isSameForMatchingInstances() {
    var idString = randomId();
    var first = new TestId(idString);
    var second = new TestId(idString);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  static class TestId extends ExternalId {
    TestId(String id) {
      super(id);
    }
  }

  static class AnotherTestId extends ExternalId {
    AnotherTestId(String id) {
      super(id);
    }
  }
}
