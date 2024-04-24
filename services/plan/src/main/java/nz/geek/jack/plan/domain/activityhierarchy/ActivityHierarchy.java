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

  public ActivityType addNestedActivityType(String name, ActivityTypeId parentId) {
    ActivityTypeId id = ActivityTypeId.create();
    apply(NestedActivityTypeAddedEvent.of(id, name, parentId));
    return activityTypes.get(id);
  }

  private void on(NestedActivityTypeAddedEvent event) {
    validateActivityTypeNameIsUnique(event.getName());
    validateActivityTypeExists(event.getParentId());

    var parent = activityTypes.get(event.getParentId());
    var child = ActivityType.create(event.getId(), event.getName());

    parent.addChild(child);
    activityTypes.put(child.getId(), child);
  }

  private void validateActivityTypeNameIsUnique(String name) {
    if (activityTypes.values().stream().anyMatch(a -> a.getName().equals(name))) {
      throw new DuplicateActivityTypeNameException();
    }
  }

  public void nestActivityType(ActivityTypeId parentId, ActivityTypeId childId) {
    apply(ActivityTypeNestedEvent.of(parentId, childId));
  }

  private void on(ActivityTypeNestedEvent event) {
    validateActivityTypeExists(event.getParentId());
    validateActivityTypeExists(event.getChildId());

    var parent = activityTypes.get(event.getParentId());
    var child = activityTypes.get(event.getChildId());

    parent.addChild(child);
  }

  private void validateActivityTypeExists(ActivityTypeId activityTypeId) {
    if (!activityTypes.containsKey(activityTypeId)) {
      throw new ActivityTypeNotFoundException(
          String.format("Activity type [%s] does not exist in hierarchy [%s]", activityTypeId, id));
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
