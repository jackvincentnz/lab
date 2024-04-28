package nz.geek.jack.mops.plan.domain.activityhierarchy;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class ActivityHierarchyCreatedEvent extends DomainEvent {

  private final ActivityHierarchyId activityHierarchyId;

  public static ActivityHierarchyCreatedEvent of(ActivityHierarchyId activityHierarchyId) {
    return new ActivityHierarchyCreatedEvent(activityHierarchyId);
  }

  private ActivityHierarchyCreatedEvent(ActivityHierarchyId activityHierarchyId) {
    this.activityHierarchyId = activityHierarchyId;
  }

  public ActivityHierarchyId getActivityHierarchyId() {
    return activityHierarchyId;
  }
}
