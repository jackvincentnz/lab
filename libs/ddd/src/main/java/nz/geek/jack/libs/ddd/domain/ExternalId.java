package nz.geek.jack.libs.ddd.domain;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public abstract class ExternalId {

  private final String id;

  protected ExternalId(String id) {
    Objects.requireNonNull(id, "id must not be null");
    validateNotBlank(id);
    this.id = id;
  }

  private void validateNotBlank(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id must not be blank");
    }
  }

  @Override
  public String toString() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalId externalId = (ExternalId) o;
    return id.equals(externalId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
