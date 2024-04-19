package nz.geek.jack.plan.domain.activityhierarchy;

import nz.geek.jack.libs.domain.DomainEvent;

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
