package nz.geek.jack.journal.domain;

import java.util.Objects;
import java.util.UUID;

public final class EntryId {

  private final UUID id;

  private EntryId() {
    this(UUID.randomUUID());
  }

  private EntryId(UUID id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  public static EntryId create() {
    return new EntryId();
  }

  public static EntryId fromString(String id) {
    return new EntryId(UUID.fromString(id));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EntryId entryId = (EntryId) o;
    return id.equals(entryId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
