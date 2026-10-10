package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.libs.ddd.domain.ExternalId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class StringToExternalIdConverterTest extends TestBase {

  @Test
  void convert_string_returnsExternalId() {
    var converter = StringToExternalIdConverter.of(TestExternalId.class);
    var id = randomString();

    var result = converter.convert(id);

    assertThat(result).isEqualTo(new TestExternalId(id));
  }

  @Test
  void convert_blankString_throwsWithValidationCause() {
    var converter = StringToExternalIdConverter.of(TestExternalId.class);

    // The constructor runs reflectively, so its validation error arrives wrapped.
    assertThatThrownBy(() -> converter.convert(" "))
        .isInstanceOf(RuntimeException.class)
        .rootCause()
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("id must not be blank");
  }

  @Test
  void convert_nullString_throwsWithNullCause() {
    var converter = StringToExternalIdConverter.of(TestExternalId.class);

    assertThatThrownBy(() -> converter.convert(null))
        .isInstanceOf(RuntimeException.class)
        .rootCause()
        .isInstanceOf(NullPointerException.class)
        .hasMessage("id must not be null");
  }

  @Test
  void convert_noStringConstructor_throws() {
    var converter = StringToExternalIdConverter.of(NoStringConstructorId.class);
    var id = randomString();

    assertThatThrownBy(() -> converter.convert(id))
        .isInstanceOf(RuntimeException.class)
        .hasCauseInstanceOf(NoSuchMethodException.class);
  }

  static class NoStringConstructorId extends ExternalId {
    NoStringConstructorId(int id) {
      super(String.valueOf(id));
    }
  }
}
