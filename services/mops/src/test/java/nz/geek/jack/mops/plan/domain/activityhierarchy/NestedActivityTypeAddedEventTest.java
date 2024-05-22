package nz.geek.jack.mops.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NestedActivityTypeAddedEventTest {

  static final String NAME = "Tactic";

  @Test
  void of_setsActivityHierarchyId() {
    var activityHierarchyId = ActivityHierarchyId.create();

    var event =
        NestedActivityTypeAddedEvent.of(
            activityHierarchyId, ActivityTypeId.create(), NAME, ActivityTypeId.create());

    assertThat(event.getAggregateId()).isEqualTo(activityHierarchyId);
  }

  @Test
  void of_setsActivityTypeId() {
    var id = ActivityTypeId.create();

    var event =
        NestedActivityTypeAddedEvent.of(
            ActivityHierarchyId.create(), id, NAME, ActivityTypeId.create());

    assertThat(event.getActivityTypeId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var event =
        NestedActivityTypeAddedEvent.of(
            ActivityHierarchyId.create(), ActivityTypeId.create(), NAME, ActivityTypeId.create());

    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsParentId() {
    var parentId = ActivityTypeId.create();

    var event =
        NestedActivityTypeAddedEvent.of(
            ActivityHierarchyId.create(), ActivityTypeId.create(), NAME, parentId);

    assertThat(event.getParentId()).isEqualTo(parentId);
  }
}
