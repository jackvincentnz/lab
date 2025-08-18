package lab.mops.core.domain.activity;

import static nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils.getLastEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ActivityTest extends TestBase {

  @Test
  void create_preventsNullName() {
    assertThatThrownBy(() -> Activity.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_setsName() {
    var name = randomString();

    var activity = Activity.create(name);

    assertThat(activity.getName()).isEqualTo(name);
  }

  @Test
  void create_setsId() {
    var activity = Activity.create(randomString());

    assertThat(activity.getId()).isNotNull();
  }

  @Test
  void createActivity_registersEvent_withId() {
    var activity = Activity.create(randomString());

    var event = getLastEvent(activity, ActivityCreatedEvent.class);
    assertThat(event.activityId()).isEqualTo(activity.getId());
  }

  @Test
  void createActivity_registersEvent_withName() {
    var name = randomString();

    var activity = Activity.create(name);

    var event = getLastEvent(activity, ActivityCreatedEvent.class);
    assertThat(event.name()).isEqualTo(name);
  }

  @Test
  void createActivity_registersEvent_withCreatedAt() {
    var activity = Activity.create(randomString());

    var event = getLastEvent(activity, ActivityCreatedEvent.class);
    assertThat(event.createdAt()).isEqualTo(activity.getCreatedAt());
  }
}
