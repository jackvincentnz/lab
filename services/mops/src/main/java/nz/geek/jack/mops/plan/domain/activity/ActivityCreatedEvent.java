package nz.geek.jack.mops.plan.domain.activity;

import java.time.Instant;
import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class ActivityCreatedEvent extends DomainEvent<ActivityId> {

  private final String name;

  private final Instant createdAt;

  public static ActivityCreatedEvent of(ActivityId activityId, String name, Instant createdAt) {
    return new ActivityCreatedEvent(activityId, name, createdAt);
  }

  private ActivityCreatedEvent(ActivityId activityId, String name, Instant createdAt) {
    super(activityId);
    this.name = name;
    this.createdAt = createdAt;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
