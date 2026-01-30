package lab.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskCompletedEventTest {

  @Test
  void of_setsTaskId() {
    var taskId = TaskId.create();

    var event = TaskCompletedEvent.of(taskId);

    assertThat(event.getAggregateId()).isEqualTo(taskId);
  }
}
