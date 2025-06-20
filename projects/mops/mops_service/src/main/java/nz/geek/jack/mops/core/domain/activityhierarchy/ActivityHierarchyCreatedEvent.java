package nz.geek.jack.mops.core.domain.activityhierarchy;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class ActivityHierarchyCreatedEvent extends DomainEvent<ActivityHierarchyId> {

  public static ActivityHierarchyCreatedEvent of(ActivityHierarchyId activityHierarchyId) {
    return new ActivityHierarchyCreatedEvent(activityHierarchyId);
  }

  private ActivityHierarchyCreatedEvent(ActivityHierarchyId activityHierarchyId) {
    super(activityHierarchyId);
  }
}
