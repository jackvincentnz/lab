package nz.geek.jack.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityTypeTest {

  static final String NAME = "Tactic";

  @Test
  void create_setsId() {
    var activityTypeId = ActivityTypeId.create();
    var activityType = ActivityType.create(activityTypeId, NAME);

    assertThat(activityType.getId()).isEqualTo(activityTypeId);
  }

  @Test
  void create_setsName() {
    var activityType = ActivityType.create(ActivityTypeId.create(), NAME);

    assertThat(activityType.getName()).isEqualTo(NAME);
  }
}
