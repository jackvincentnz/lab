package nz.geek.jack.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskAddedEventTest {

  @Test
  void of_setsId() {
    var id = TaskId.create();
    var title = "My Task";

    var event = TaskAddedEvent.of(id, title);

    assertThat(event.getTaskId()).isEqualTo(id);
  }

  @Test
  void of_setsTitle() {
    var id = TaskId.create();
    var title = "My Task";

    var event = TaskAddedEvent.of(id, title);

    assertThat(event.getTitle()).isEqualTo(title);
  }
}
