package nz.geek.jack.mops.core.domain.activityhierarchy;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class NestedActivityTypeAddedEvent extends DomainEvent<ActivityHierarchyId> {

  private final ActivityTypeId activityTypeId;

  private final String name;

  private final ActivityTypeId parentId;

  public static NestedActivityTypeAddedEvent of(
      ActivityHierarchyId activityHierarchyId,
      ActivityTypeId activityTypeId,
      String name,
      ActivityTypeId parentId) {
    return new NestedActivityTypeAddedEvent(activityHierarchyId, activityTypeId, name, parentId);
  }

  private NestedActivityTypeAddedEvent(
      ActivityHierarchyId activityHierarchyId,
      ActivityTypeId activityTypeId,
      String name,
      ActivityTypeId parentId) {
    super(activityHierarchyId);
    this.activityTypeId = activityTypeId;
    this.name = name;
    this.parentId = parentId;
  }

  public ActivityTypeId getActivityTypeId() {
    return activityTypeId;
  }

  public String getName() {
    return name;
  }

  public ActivityTypeId getParentId() {
    return parentId;
  }
}
