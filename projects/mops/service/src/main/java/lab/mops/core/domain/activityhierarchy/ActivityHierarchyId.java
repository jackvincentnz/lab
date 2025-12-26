package lab.mops.core.domain.activityhierarchy;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.InternalId;

public final class ActivityHierarchyId extends InternalId {

  private ActivityHierarchyId() {
    super();
  }

  private ActivityHierarchyId(UUID id) {
    super(id);
  }

  public static ActivityHierarchyId create() {
    return new ActivityHierarchyId();
  }

  public static ActivityHierarchyId fromString(String id) {
    return new ActivityHierarchyId(UUID.fromString(id));
  }
}
