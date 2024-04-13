package nz.geek.jack.plan.domain.activitytype;

import java.time.Instant;
import nz.geek.jack.libs.domain.DomainEvent;

public final class ActivityTypeCreatedEvent extends DomainEvent {

  private final ActivityTypeId activityTypeId;

  private final String name;

  private final Instant createdAt;

  public static ActivityTypeCreatedEvent of(
      ActivityTypeId activityTypeId, String name, Instant createdAt) {
    return new ActivityTypeCreatedEvent(activityTypeId, name, createdAt);
  }

  private ActivityTypeCreatedEvent(ActivityTypeId activityTypeId, String name, Instant createdAt) {
    this.activityTypeId = activityTypeId;
    this.name = name;
    this.createdAt = createdAt;
  }

  public ActivityTypeId getActivityTypeId() {
    return activityTypeId;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
