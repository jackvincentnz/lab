package nz.geek.jack.libs.ddd.domain;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractId {

  private final UUID id;

  protected AbstractId() {
    this(UUID.randomUUID());
  }

  protected AbstractId(UUID id) {
    this.id = id;
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
    AbstractId abstractId = (AbstractId) o;
    return id.equals(abstractId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
