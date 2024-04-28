package nz.geek.jack.mops.plan.domain.activityhierarchy;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

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
