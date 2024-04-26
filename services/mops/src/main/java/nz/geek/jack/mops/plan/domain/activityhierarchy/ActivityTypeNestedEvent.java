package nz.geek.jack.mops.plan.domain.activityhierarchy;

import nz.geek.jack.libs.domain.DomainEvent;

public class ActivityTypeNestedEvent extends DomainEvent {

  private final ActivityTypeId parentId;

  private final ActivityTypeId childId;

  public static ActivityTypeNestedEvent of(ActivityTypeId parentId, ActivityTypeId childId) {
    return new ActivityTypeNestedEvent(parentId, childId);
  }

  private ActivityTypeNestedEvent(ActivityTypeId parentId, ActivityTypeId childId) {
    this.parentId = parentId;
    this.childId = childId;
  }

  public ActivityTypeId getParentId() {
    return parentId;
  }

  public ActivityTypeId getChildId() {
    return childId;
  }
}
