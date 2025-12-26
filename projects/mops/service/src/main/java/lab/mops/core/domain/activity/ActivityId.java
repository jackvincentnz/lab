package lab.mops.core.domain.activity;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.InternalId;

public final class ActivityId extends InternalId {

  private ActivityId() {
    super();
  }

  private ActivityId(UUID id) {
    super(id);
  }

  public static ActivityId create() {
    return new ActivityId();
  }

  public static ActivityId fromString(String id) {
    return new ActivityId(UUID.fromString(id));
  }
}
