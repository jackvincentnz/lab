package lab.mops.core.domain.activity;

import static nz.geek.jack.libs.ddd.domain.test.ESAggregateTestUtils.getOnlyEventOfType;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ActivityTest {

  static final String NAME = "Intro to Lab Webinar";

  @Test
  void createActivity_setsName() {
    var activity = Activity.createActivity(NAME);

    assertThat(activity.getName()).isEqualTo(NAME);
  }

  @Test
  void createActivity_setsId() {
    var activity = Activity.createActivity(NAME);

    assertThat(activity.getId()).isNotNull();
  }

  @Test
  void createActivity_setsCreatedAt() {
    var activity = Activity.createActivity(NAME);

    assertThat(activity.getCreatedAt()).isBetween(Instant.now().minusSeconds(10), Instant.now());
  }

  @Test
  void createActivity_appliesActivityCreatedEvent_withId() {
    var activity = Activity.createActivity(NAME);

    var event = getOnlyEventOfType(activity, ActivityCreatedEvent.class);
    assertThat(event.getAggregateId()).isEqualTo(activity.getId());
  }

  @Test
  void createActivity_appliesActivityCreatedEvent_withName() {
    var activity = Activity.createActivity(NAME);

    var event = getOnlyEventOfType(activity, ActivityCreatedEvent.class);
    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void createActivity_appliesActivityCreatedEvent_withCreatedAt() {
    var activity = Activity.createActivity(NAME);

    var event = getOnlyEventOfType(activity, ActivityCreatedEvent.class);
    assertThat(event.getCreatedAt()).isEqualTo(activity.getCreatedAt());
  }

  @Test
  void of_setsId() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var activity = Activity.of(id, NAME, createdAt);

    assertThat(activity.getId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var activity = Activity.of(id, NAME, createdAt);

    assertThat(activity.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsCreatedAt() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var activity = Activity.of(id, NAME, createdAt);

    assertThat(activity.getCreatedAt()).isEqualTo(createdAt);
  }
}
