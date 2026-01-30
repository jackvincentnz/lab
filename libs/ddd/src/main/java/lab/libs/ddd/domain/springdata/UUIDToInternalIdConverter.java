package lab.libs.ddd.domain.springdata;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UUIDToInternalIdConverter<T extends InternalId> implements Converter<UUID, T> {

  private final Class<? extends T> clazz;

  private UUIDToInternalIdConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  public static <T extends InternalId> UUIDToInternalIdConverter<T> of(Class<T> clazz) {
    return new UUIDToInternalIdConverter<>(clazz);
  }

  @Override
  public T convert(UUID source) {
    try {
      var constructor = clazz.getDeclaredConstructor(UUID.class);
      constructor.setAccessible(true);
      return constructor.newInstance(source);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
