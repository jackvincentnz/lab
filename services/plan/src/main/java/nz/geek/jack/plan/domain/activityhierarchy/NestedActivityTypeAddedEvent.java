package nz.geek.jack.plan.domain.activityhierarchy;

import nz.geek.jack.libs.domain.DomainEvent;

public final class NestedActivityTypeAddedEvent extends DomainEvent {

  private final ActivityTypeId id;

  private final String name;

  private final ActivityTypeId parentId;

  public static NestedActivityTypeAddedEvent of(
      ActivityTypeId id, String name, ActivityTypeId parentId) {
    return new NestedActivityTypeAddedEvent(id, name, parentId);
  }

  private NestedActivityTypeAddedEvent(ActivityTypeId id, String name, ActivityTypeId parentId) {
    this.id = id;
    this.name = name;
    this.parentId = parentId;
  }

  public ActivityTypeId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public ActivityTypeId getParentId() {
    return parentId;
  }
}
