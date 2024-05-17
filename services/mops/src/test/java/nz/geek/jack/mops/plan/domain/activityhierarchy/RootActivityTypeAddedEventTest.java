package nz.geek.jack.mops.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RootActivityTypeAddedEventTest {

  static final String NAME = "Tactic";

  @Test
  void of_setsActivityHierarchyId() {
    var activityHierarchyId = ActivityHierarchyId.create();

    var event = RootActivityTypeAddedEvent.of(activityHierarchyId, ActivityTypeId.create(), NAME);

    assertThat(event.getAggregateId()).isEqualTo(activityHierarchyId);
  }

  @Test
  void of_setsActivityTypeId() {
    var activityTypeId = ActivityTypeId.create();

    var event = RootActivityTypeAddedEvent.of(ActivityHierarchyId.create(), activityTypeId, NAME);

    assertThat(event.getActivityTypeId()).isEqualTo(activityTypeId);
  }

  @Test
  void of_setsName() {
    var event =
        RootActivityTypeAddedEvent.of(ActivityHierarchyId.create(), ActivityTypeId.create(), NAME);

    assertThat(event.getName()).isEqualTo(NAME);
  }
}
