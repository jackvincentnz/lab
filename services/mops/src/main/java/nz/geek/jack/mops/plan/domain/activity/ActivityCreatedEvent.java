package nz.geek.jack.mops.plan.domain.activity;

import java.time.Instant;
import nz.geek.jack.libs.domain.DomainEvent;

public final class ActivityCreatedEvent extends DomainEvent {

  private final ActivityId activityId;

  private final String name;

  private final Instant createdAt;

  public static ActivityCreatedEvent of(ActivityId activityId, String name, Instant createdAt) {
    return new ActivityCreatedEvent(activityId, name, createdAt);
  }

  private ActivityCreatedEvent(ActivityId activityId, String name, Instant createdAt) {
    this.activityId = activityId;
    this.name = name;
    this.createdAt = createdAt;
  }

  public ActivityId getActivityId() {
    return activityId;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
