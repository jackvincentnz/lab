package nz.geek.jack.journal.domain;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class EntryId extends AbstractId {

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
