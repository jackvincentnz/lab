package lab.libs.ddd.domain.springdata;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UUIDToInternalIdConverterFactory implements ConverterFactory<UUID, InternalId> {

  @Override
  public <T extends InternalId> Converter<UUID, T> getConverter(Class<T> targetType) {
    return UUIDToInternalIdConverter.of(targetType);
  }
}
