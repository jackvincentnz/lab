package nz.geek.jack.libs.ddd.domain.springdata;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class AbstractIdToUUIDConverter implements Converter<AbstractId, UUID> {

  @Override
  public UUID convert(AbstractId source) {
    return source.toUUID();
  }
}
