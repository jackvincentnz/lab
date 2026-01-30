package lab.mops.core.domain.category;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class CategoryId extends InternalId {

  private CategoryId() {
    super();
  }

  private CategoryId(UUID id) {
    super(id);
  }

  public static CategoryId create() {
    return new CategoryId();
  }

  public static CategoryId fromString(String id) {
    return new CategoryId(UUID.fromString(id));
  }
}
