package nz.geek.jack.plan.domain.activity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ActivityCreatedEventTest {

  static final String NAME = "Intro to Lab Webinar";

  @Test
  void of_setsId() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var event = ActivityCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getActivityId()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var event = ActivityCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getName()).isEqualTo(NAME);
  }

  @Test
  void of_setsCreatedAt() {
    var id = ActivityId.create();
    var createdAt = Instant.now();

    var event = ActivityCreatedEvent.of(id, NAME, createdAt);

    assertThat(event.getCreatedAt()).isEqualTo(createdAt);
  }
}
