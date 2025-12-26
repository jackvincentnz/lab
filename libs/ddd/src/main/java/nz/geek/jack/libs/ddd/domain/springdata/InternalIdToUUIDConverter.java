package nz.geek.jack.libs.ddd.domain.springdata;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.InternalId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class InternalIdToUUIDConverter implements Converter<InternalId, UUID> {

  @Override
  public UUID convert(InternalId source) {
    return source.toUUID();
  }
}
