package nz.geek.jack.plan.domain.activityhierarchy;

import nz.geek.jack.libs.domain.DomainEvent;

public final class ActivityTypeAddedEvent extends DomainEvent {

  private final ActivityTypeId activityTypeId;

  private final String name;

  public static ActivityTypeAddedEvent of(ActivityTypeId activityTypeId, String name) {
    return new ActivityTypeAddedEvent(activityTypeId, name);
  }

  private ActivityTypeAddedEvent(ActivityTypeId activityTypeId, String name) {
    this.activityTypeId = activityTypeId;
    this.name = name;
  }

  public ActivityTypeId getActivityTypeId() {
    return activityTypeId;
  }

  public String getName() {
    return name;
  }
}
