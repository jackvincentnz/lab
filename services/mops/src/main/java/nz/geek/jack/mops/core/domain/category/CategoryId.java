package nz.geek.jack.mops.core.domain.category;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class CategoryId extends AbstractId {

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
