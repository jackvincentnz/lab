package lab.mops.core.domain.budget;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class LineItemId extends InternalId {

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
