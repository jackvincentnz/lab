package nz.geek.jack.plan.domain.activityhierarchy;

import nz.geek.jack.libs.domain.DomainEvent;

public final class RootActivityTypeAddedEvent extends DomainEvent {

  private final ActivityTypeId activityTypeId;

  private final String name;

  public static RootActivityTypeAddedEvent of(ActivityTypeId activityTypeId, String name) {
    return new RootActivityTypeAddedEvent(activityTypeId, name);
  }

  private RootActivityTypeAddedEvent(ActivityTypeId activityTypeId, String name) {
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
