package nz.geek.jack.journal.adapter.api.gql;

import nz.geek.jack.journal.adapter.gql.schema.types.Entry;
import org.springframework.stereotype.Component;

@Component
public class EntryMapper {
  public Entry map(nz.geek.jack.journal.domain.Entry entry) {
    return new Entry(
        entry.getId().toString(), entry.getMessage(), entry.getCreatedAt().toString(), null);
  }
}
