package nz.geek.jack.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityTypeNestedEventTest {

  @Test
  void of_setsParentId() {
    var parentId = ActivityTypeId.create();

    var event = ActivityTypeNestedEvent.of(parentId, ActivityTypeId.create());

    assertThat(event.getParentId()).isEqualTo(parentId);
  }

  @Test
  void of_setsChildId() {
    var childId = ActivityTypeId.create();

    var event = ActivityTypeNestedEvent.of(ActivityTypeId.create(), childId);

    assertThat(event.getChildId()).isEqualTo(childId);
  }
}
