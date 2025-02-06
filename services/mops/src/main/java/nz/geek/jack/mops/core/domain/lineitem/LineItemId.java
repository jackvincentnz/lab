package nz.geek.jack.mops.core.domain.lineitem;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class LineItemId extends AbstractId {

  private LineItemId() {
    super();
  }

  private LineItemId(UUID id) {
    super(id);
  }

  public static LineItemId create() {
    return new LineItemId();
  }

  public static LineItemId fromString(String id) {
    return new LineItemId(UUID.fromString(id));
  }
}
