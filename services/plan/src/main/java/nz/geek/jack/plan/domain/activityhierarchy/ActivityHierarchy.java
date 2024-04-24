package nz.geek.jack.plan.domain.activityhierarchy;

import java.util.HashMap;
import java.util.Optional;
import nz.geek.jack.libs.domain.Aggregate;

public final class ActivityHierarchy extends Aggregate {

  private ActivityHierarchyId id;

  private ActivityType rootActivityType;

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

  public ActivityType addRootActivityType(String name) {
    apply(RootActivityTypeAddedEvent.of(ActivityTypeId.create(), name));
    return rootActivityType;
  }

  private void on(RootActivityTypeAddedEvent event) {
    validateEmptyHierarchy();

    var activityType = ActivityType.create(event.getActivityTypeId(), event.getName());

    rootActivityType = activityType;
    activityTypes.put(activityType.getId(), activityType);
  }

  private void validateEmptyHierarchy() {
    if (rootActivityType != null) {
      throw new NotEmptyHierarchyException(String.format("The hierarchy [%s] is not empty", id));
    }
  }

  public ActivityHierarchyId getId() {
    return id;
  }

  Optional<ActivityType> getRootActivityType() {
    return Optional.ofNullable(rootActivityType);
  }

  int getActivityTypeCount() {
    return activityTypes.size();
  }
}
