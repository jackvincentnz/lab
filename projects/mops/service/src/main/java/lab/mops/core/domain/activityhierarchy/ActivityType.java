package lab.mops.core.domain.activityhierarchy;

import java.util.HashSet;
import java.util.Set;

public final class ActivityType {

  private final ActivityTypeId id;

  private String name;

  private final Set<ActivityType> children = new HashSet<>();

  static ActivityType create(ActivityTypeId activityTypeId, String name) {
    return new ActivityType(activityTypeId, name);
  }

  private ActivityType(ActivityTypeId id, String name) {
    this.id = id;
    this.name = name;
  }

  void addChild(ActivityType child) {
    this.children.add(child);
  }

  public ActivityTypeId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  boolean hasChild(ActivityType child) {
    return children.contains(child);
  }
}
