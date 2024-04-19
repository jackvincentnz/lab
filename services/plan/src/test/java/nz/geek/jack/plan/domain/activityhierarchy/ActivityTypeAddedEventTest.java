package nz.geek.jack.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityTypeAddedEventTest {

  static final String NAME = "Tactic";

  @Test
  void of_setsActivityTypeId() {
    var activityTypeId = ActivityTypeId.create();
    var event = ActivityTypeAddedEvent.of(activityTypeId, NAME);

    assertThat(event.getActivityTypeId()).isEqualTo(activityTypeId);
  }

  @Test
  void of_setsName() {
    var event = ActivityTypeAddedEvent.of(ActivityTypeId.create(), NAME);

    assertThat(event.getName()).isEqualTo(NAME);
  }
}
