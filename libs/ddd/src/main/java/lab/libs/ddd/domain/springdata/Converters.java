package lab.libs.ddd.domain.springdata;

import java.util.List;

public class Converters {

  public static List<?> allConverters() {
    return List.of(
        new InternalIdToUUIDConverter(),
        new UUIDToInternalIdConverterFactory(),
        new StringValueToStringConverter(),
        new StringToStringValueConverterFactory(),
        new ExternalIdToStringConverter(),
        new StringToExternalIdConverterFactory());
  }
}
