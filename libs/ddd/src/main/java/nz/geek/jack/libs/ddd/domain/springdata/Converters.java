package nz.geek.jack.libs.ddd.domain.springdata;

import java.util.List;

public class Converters {

  public static List<?> allConverters() {
    return List.of(
        new AbstractIdToUUIDConverter(),
        new UUIDToAbstractIdConverterFactory(),
        new StringValueToStringConverter(),
        new StringToStringValueConverterFactory());
  }
}
