package nz.geek.jack.libs.ddd.domain;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

public class StringValueTest extends TestBase {

  @Test
  void equals_trueWhenValueMatches() {
    var a = new TestValue("1");
    var b = new TestValue("1");

    assertThat(a).isEqualTo(b);
  }

  @Test
  void equals_falseWhenValueDifferent() {
    var a = new TestValue("1");
    var b = new TestValue("2");

    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void equals_falseIfNull() {
    var a = new TestValue(randomString());

    assertThat(a).isNotEqualTo(null);
  }

  @Test
  void equals_falseIfOtherClass() {
    var a = new TestValue(randomString());

    assertThat(a).isNotEqualTo(new Object());
  }

  @Test
  void hashCode_matchesForSameValue() {
    var a = new TestValue("1");
    var b = new TestValue("1");

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void toString_returnsValue() {
    var value = randomString();

    var result = new TestValue(value);

    assertThat(result.toString()).isEqualTo(value);
  }

  static class TestValue extends StringValue {
    TestValue(String value) {
      super(value);
    }
  }
}
