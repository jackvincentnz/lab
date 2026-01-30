package lab.mops.core.domain.activityhierarchy;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class ActivityTypeId extends InternalId {

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
