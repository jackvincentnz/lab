package nz.geek.jack.libs.ddd.domain.springdata;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UUIDToAbstractIdConverterFactory implements ConverterFactory<UUID, AbstractId> {

  @Override
  public <T extends AbstractId> Converter<UUID, T> getConverter(Class<T> targetType) {
    return UUIDToAbstractIdConverter.of(targetType);
  }
}
