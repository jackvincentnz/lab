package nz.geek.jack.mops.core.domain.activityhierarchy;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class RootActivityTypeAddedEvent extends DomainEvent<ActivityHierarchyId> {

  private final ActivityTypeId activityTypeId;

  private final String name;

  public static RootActivityTypeAddedEvent of(
      ActivityHierarchyId activityHierarchyId, ActivityTypeId activityTypeId, String name) {
    return new RootActivityTypeAddedEvent(activityHierarchyId, activityTypeId, name);
  }

  private RootActivityTypeAddedEvent(
      ActivityHierarchyId activityHierarchyId, ActivityTypeId activityTypeId, String name) {
    super(activityHierarchyId);
    this.activityTypeId = activityTypeId;
    this.name = name;
  }

  public ActivityTypeId getActivityTypeId() {
    return activityTypeId;
  }

  public String getName() {
    return name;
  }
}
