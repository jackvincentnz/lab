package lab.libs.ddd.domain.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ConvertersTest extends TestBase {

  @Test
  void allConverters_called_returnsReadingAndWritingConvertersForEachValueType() {
    var converters = Converters.allConverters();

    assertThat(converters)
        .extracting(Object::getClass)
        .containsExactlyInAnyOrder(
            InternalIdToUUIDConverter.class,
            UUIDToInternalIdConverterFactory.class,
            StringValueToStringConverter.class,
            StringToStringValueConverterFactory.class,
            ExternalIdToStringConverter.class,
            StringToExternalIdConverterFactory.class);
  }
}
