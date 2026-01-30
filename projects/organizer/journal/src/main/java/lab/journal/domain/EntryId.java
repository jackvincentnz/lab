package lab.journal.domain;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class EntryId extends InternalId {

  private EntryId() {
    super();
  }

  private EntryId(UUID id) {
    super(id);
  }

  public static EntryId create() {
    return new EntryId();
  }

  public static EntryId fromString(String id) {
    return new EntryId(UUID.fromString(id));
  }
}
