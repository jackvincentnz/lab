package nz.geek.jack.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TaskAddedEventTest {

  @Test
  void of_setsId() {
    var id = TaskId.create();
    var title = "My Task";
    var createdAt = Instant.now();

    var event = TaskAddedEvent.of(id, title, createdAt);

    assertThat(event.getTaskId()).isEqualTo(id);
  }

  @Test
  void of_setsTitle() {
    var id = TaskId.create();
    var title = "My Task";
    var createdAt = Instant.now();

    var event = TaskAddedEvent.of(id, title, createdAt);

    assertThat(event.getTitle()).isEqualTo(title);
  }

  @Test
  void of_setsCreatedAt() {
    var id = TaskId.create();
    var title = "My Task";
    var createdAt = Instant.now();

    var event = TaskAddedEvent.of(id, title, createdAt);

    assertThat(event.getCreatedAt()).isEqualTo(createdAt);
  }
}
