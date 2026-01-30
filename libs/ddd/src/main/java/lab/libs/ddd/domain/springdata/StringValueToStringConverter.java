package lab.libs.ddd.domain.springdata;

import lab.libs.ddd.domain.StringValue;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class StringValueToStringConverter implements Converter<StringValue, String> {

  @Override
  public String convert(StringValue source) {
    return source.toString();
  }
}
