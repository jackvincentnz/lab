package lab.libs.ddd.domain.springdata;

import lab.libs.ddd.domain.StringValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringToStringValueConverterFactory implements ConverterFactory<String, StringValue> {

  @Override
  public <T extends StringValue> Converter<String, T> getConverter(Class<T> targetType) {
    return StringToStringValueConverter.of(targetType);
  }
}
