package nz.geek.jack.mops.core.domain.activityhierarchy;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class ActivityTypeId extends AbstractId {

  private ActivityTypeId() {
    super();
  }

  private ActivityTypeId(UUID id) {
    super(id);
  }

  public static ActivityTypeId create() {
    return new ActivityTypeId();
  }

  public static ActivityTypeId fromString(String id) {
    return new ActivityTypeId(UUID.fromString(id));
  }
}
