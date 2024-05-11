package nz.geek.jack.libs.ddd.domain.springdata;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UUIDToAbstractIdConverter<T extends AbstractId> implements Converter<UUID, T> {

  private final Class<? extends T> clazz;

  private UUIDToAbstractIdConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  public static <T extends AbstractId> UUIDToAbstractIdConverter<T> of(Class<T> clazz) {
    return new UUIDToAbstractIdConverter<>(clazz);
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
