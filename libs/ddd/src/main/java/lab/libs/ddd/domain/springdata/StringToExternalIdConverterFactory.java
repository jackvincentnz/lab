package lab.libs.ddd.domain.springdata;

import lab.libs.ddd.domain.ExternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToExternalIdConverterFactory implements ConverterFactory<String, ExternalId> {

  @Override
  public <T extends ExternalId> Converter<String, T> getConverter(Class<T> targetType) {
    return StringToExternalIdConverter.of(targetType);
  }
}
