package nz.geek.jack.mops.core.domain.category;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public class CategoryValueId extends AbstractId {

  private CategoryValueId() {
    super();
  }

  private CategoryValueId(UUID id) {
    super(id);
  }

  public static CategoryValueId create() {
    return new CategoryValueId();
  }

  public static CategoryValueId fromString(String id) {
    return new CategoryValueId(UUID.fromString(id));
  }
}
