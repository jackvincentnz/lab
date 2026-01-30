package lab.journal.adapter.api.gql;

import lab.journal.adapter.gql.schema.types.Entry;
import org.springframework.stereotype.Component;

@Component
public class EntryMapper {
  public Entry map(lab.journal.domain.Entry entry) {
    return new Entry(
        entry.getId().toString(), entry.getMessage(), entry.getCreatedAt().toString(), null);
  }
}
