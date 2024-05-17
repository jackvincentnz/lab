package nz.geek.jack.mops.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityHierarchyCreatedEventTest {

  @Test
  void of_setsId() {
    var id = ActivityHierarchyId.create();

    var event = ActivityHierarchyCreatedEvent.of(id);

    assertThat(event.getAggregateId()).isEqualTo(id);
  }
}
