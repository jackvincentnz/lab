package nz.geek.jack.plan.domain.activityhierarchy;

import java.util.HashMap;
import nz.geek.jack.libs.domain.Aggregate;

public final class ActivityHierarchy extends Aggregate {

  private ActivityHierarchyId id;

  private final HashMap<ActivityTypeId, ActivityType> activityTypes = new HashMap<>();

  public static ActivityHierarchy create() {
    return new ActivityHierarchy();
  }

  private ActivityHierarchy() {
    apply(ActivityHierarchyCreatedEvent.of(ActivityHierarchyId.create()));
  }

  private void on(ActivityHierarchyCreatedEvent event) {
    id = event.getActivityHierarchyId();
  }

  public void addActivityType(String name) {
    apply(ActivityTypeAddedEvent.of(ActivityTypeId.create(), name));
  }

  private void on(ActivityTypeAddedEvent event) {
    var activityType = ActivityType.create(event.getActivityTypeId(), event.getName());
    activityTypes.put(activityType.getId(), activityType);
  }

  public ActivityHierarchyId getId() {
    return id;
  }

  int getActivityTypeCount() {
    return activityTypes.size();
  }
}
