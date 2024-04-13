package nz.geek.jack.plan.domain.activitytype;

import java.time.Instant;
import nz.geek.jack.libs.domain.Aggregate;

public final class ActivityType extends Aggregate {

  private ActivityTypeId id;

  private String name;

  private Instant createdAt;

  public static ActivityType createActivityType(String name) {
    return new ActivityType(name);
  }

  private ActivityType(String name) {
    apply(ActivityTypeCreatedEvent.of(ActivityTypeId.create(), name, Instant.now()));
  }

  private void on(ActivityTypeCreatedEvent activityCreatedEvent) {
    id = activityCreatedEvent.getActivityTypeId();
    name = activityCreatedEvent.getName();
    createdAt = activityCreatedEvent.getCreatedAt();
  }

  public ActivityTypeId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public static ActivityType of(ActivityTypeId id, String name, Instant createdAt) {
    return new ActivityType(id, name, createdAt);
  }

  private ActivityType(ActivityTypeId id, String name, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
  }
}
