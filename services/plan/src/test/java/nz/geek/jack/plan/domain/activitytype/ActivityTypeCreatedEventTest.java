package nz.geek.jack.plan.domain.activitytype;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ActivityTypeCreatedEventTest {

  static final String NAME = "Tactic";

  @Test
  void of_setsId() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var event = ActivityTypeCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getActivityTypeId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var event = ActivityTypeCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsCreatedAt() {
    var id = ActivityTypeId.create();
    var createdAt = Instant.now();

    var event = ActivityTypeCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getCreatedAt()).isEqualTo(createdAt);
  }
}
