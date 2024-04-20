package nz.geek.jack.plan.domain.activityhierarchy;

public final class ActivityType {

  private final ActivityTypeId id;

  private String name;

  static ActivityType create(ActivityTypeId activityTypeId, String name) {
    return new ActivityType(activityTypeId, name);
  }

  private ActivityType(ActivityTypeId id, String name) {
    this.id = id;
    this.name = name;
  }

  public ActivityTypeId getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
