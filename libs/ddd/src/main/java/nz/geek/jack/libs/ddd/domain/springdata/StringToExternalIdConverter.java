package nz.geek.jack.libs.ddd.domain.springdata;

import nz.geek.jack.libs.ddd.domain.ExternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToExternalIdConverter<T extends ExternalId> implements Converter<String, T> {

  private final Class<? extends T> clazz;

  private StringToExternalIdConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  public static <T extends ExternalId> StringToExternalIdConverter<T> of(Class<T> clazz) {
    return new StringToExternalIdConverter<>(clazz);
  }

  @Override
  public T convert(String source) {
    try {
      var constructor = clazz.getDeclaredConstructor(String.class);
      constructor.setAccessible(true);
      return constructor.newInstance(source);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
