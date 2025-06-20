package lab.mops.core.domain.activityhierarchy;

import java.util.HashMap;
import java.util.Optional;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;

public final class ActivityHierarchy extends EventSourcedAggregate<ActivityHierarchyId> {

  private final HashMap<ActivityTypeId, ActivityType> activityTypes = new HashMap<>();

  private ActivityType rootActivityType;

  private ActivityHierarchy() {
    apply(ActivityHierarchyCreatedEvent.of(ActivityHierarchyId.create()));
  }

  public ActivityType addRootActivityType(String name) {
    apply(RootActivityTypeAddedEvent.of(id, ActivityTypeId.create(), name));
    return rootActivityType;
  }

  public ActivityType addNestedActivityType(String name, ActivityTypeId parentId) {
    ActivityTypeId activityTypeId = ActivityTypeId.create();
    apply(NestedActivityTypeAddedEvent.of(id, activityTypeId, name, parentId));
    return activityTypes.get(activityTypeId);
  }

  private void on(ActivityHierarchyCreatedEvent event) {
    id = event.getAggregateId();
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

  private void on(NestedActivityTypeAddedEvent event) {
    validateActivityTypeNameIsUnique(event.getName());
    validateActivityTypeExists(event.getParentId());

    var parent = activityTypes.get(event.getParentId());
    var child = ActivityType.create(event.getActivityTypeId(), event.getName());

    parent.addChild(child);
    activityTypes.put(child.getId(), child);
  }

  private void validateActivityTypeNameIsUnique(String name) {
    if (activityTypes.values().stream().anyMatch(a -> a.getName().equals(name))) {
      throw new DuplicateActivityTypeNameException();
    }
  }

  private void validateActivityTypeExists(ActivityTypeId activityTypeId) {
    if (!activityTypes.containsKey(activityTypeId)) {
      throw new ActivityTypeNotFoundException(
          String.format("Activity type [%s] does not exist in hierarchy [%s]", activityTypeId, id));
    }
  }

  Optional<ActivityType> getRootActivityType() {
    return Optional.ofNullable(rootActivityType);
  }

  int getActivityTypeCount() {
    return activityTypes.size();
  }

  public static ActivityHierarchy create() {
    return new ActivityHierarchy();
  }
}
