package nz.geek.jack.plan.domain.activitytype;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ActivityTypeTest {

  static final String NAME = "Tactic";

  @Test
  void createActivityType_setsName() {
    var activityType = ActivityType.createActivityType(NAME);

    assertThat(activityType.getName()).isEqualTo(NAME);
  }

  @Test
  void createActivityType_setsId() {
    var activityType = ActivityType.createActivityType(NAME);

    assertThat(activityType.getId()).isNotNull();
  }

  @Test
  void createActivityType_setsCreatedAt() {
    var activityType = ActivityType.createActivityType(NAME);

    assertThat(activityType.getCreatedAt())
        .isBetween(Instant.now().minusSeconds(10), Instant.now());
  }

  @Test
  void createActivityType_appliesActivityTypeCreatedEvent() {
    var activityType = ActivityType.createActivityType(NAME);

    var events = activityType.flushEvents();
    var event = events.get(0);

    assertThat(events.size()).isEqualTo(1);
    assertThat(event).isInstanceOf(ActivityTypeCreatedEvent.class);
  }

  @Test
  void createActivityType_appliesActivityTypeCreatedEvent_withId() {
    var activityType = ActivityType.createActivityType(NAME);

    var events = activityType.flushEvents();
    var event = (ActivityTypeCreatedEvent) events.get(0);

    assertThat(event.getActivityTypeId()).isEqualTo(activityType.getId());
  }

  @Test
  void createActivityType_appliesActivityTypeCreatedEvent_withName() {
    var activityType = ActivityType.createActivityType(NAME);

    var events = activityType.flushEvents();
    var event = (ActivityTypeCreatedEvent) events.get(0);

    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void createActivityType_appliesActivityTypeCreatedEvent_withCreatedAt() {
    var activityType = ActivityType.createActivityType(NAME);

    var events = activityType.flushEvents();
    var event = (ActivityTypeCreatedEvent) events.get(0);

    assertThat(event.getCreatedAt()).isEqualTo(activityType.getCreatedAt());
  }

  @Test
  void of_setsId() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var activityType = ActivityType.of(id, NAME, createdAt);

    assertThat(activityType.getId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var activityType = ActivityType.of(id, NAME, createdAt);

    assertThat(activityType.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsCreatedAt() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var activityType = ActivityType.of(id, NAME, createdAt);

    assertThat(activityType.getCreatedAt()).isEqualTo(createdAt);
  }
}
