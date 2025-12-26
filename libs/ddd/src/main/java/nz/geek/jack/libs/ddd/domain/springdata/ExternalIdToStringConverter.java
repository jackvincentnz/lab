package nz.geek.jack.libs.ddd.domain.springdata;

import nz.geek.jack.libs.ddd.domain.ExternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class ExternalIdToStringConverter implements Converter<ExternalId, String> {

  @Override
  public String convert(ExternalId source) {
    return source.toString();
  }
}
