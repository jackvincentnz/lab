package nz.geek.jack.mops.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NestedActivityTypeAddedEventTest {

  static final String NAME = "Tactic";

  @Test
  void of_setsId() {
    var id = ActivityTypeId.create();

    var event = NestedActivityTypeAddedEvent.of(id, NAME, ActivityTypeId.create());

    assertThat(event.getId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var event =
        NestedActivityTypeAddedEvent.of(ActivityTypeId.create(), NAME, ActivityTypeId.create());

    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsParentId() {
    var parentId = ActivityTypeId.create();

    var event = NestedActivityTypeAddedEvent.of(ActivityTypeId.create(), NAME, parentId);

    assertThat(event.getParentId()).isEqualTo(parentId);
  }
}
