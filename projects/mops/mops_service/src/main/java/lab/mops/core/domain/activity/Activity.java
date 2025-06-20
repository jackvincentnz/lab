package lab.mops.core.domain.activity;

import java.time.Instant;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;

public final class Activity extends EventSourcedAggregate<ActivityId> {

  private String name;

  private Instant createdAt;

  public static Activity createActivity(String name) {
    return new Activity(name);
  }

  private Activity(String name) {
    apply(ActivityCreatedEvent.of(ActivityId.create(), name, Instant.now()));
  }

  private void on(ActivityCreatedEvent activityCreatedEvent) {
    id = activityCreatedEvent.getAggregateId();
    name = activityCreatedEvent.getName();
    createdAt = activityCreatedEvent.getCreatedAt();
  }

  public String getName() {
    return name;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public static Activity of(ActivityId id, String name, Instant createdAt) {
    return new Activity(id, name, createdAt);
  }

  private Activity(ActivityId id, String name, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.createdAt = createdAt;
  }
}
