package nz.geek.jack.libs.ddd.domain;

import java.util.Objects;
import java.util.UUID;

public abstract class InternalId {

  private final UUID id;

  protected InternalId() {
    this(UUID.randomUUID());
  }

  protected InternalId(UUID id) {
    Objects.requireNonNull(id);
    this.id = id;
  }

  public UUID toUUID() {
    return id;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InternalId internalId = (InternalId) o;
    return id.equals(internalId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
