package lab.mops.core.domain.activity;

import java.util.Objects;
import lab.libs.ddd.domain.Aggregate;

public final class Activity extends Aggregate<ActivityId> {

  private String name;

  private Activity(ActivityId id, String name) {
    super(id);
    Objects.requireNonNull(name, "Name cannot be null");
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static Activity create(String name) {
    var activity = new Activity(ActivityId.create(), name);

    activity.registerEvent(
        new ActivityCreatedEvent(activity.id, activity.name, activity.createdAt));

    return activity;
  }
}
