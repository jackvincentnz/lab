package lab.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import lab.task.domain.TaskCompletedEvent;
import lab.task.domain.TaskId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class TaskCompletedEventMapperTest extends TestBase {

  TaskCompletedEventMapper taskCompletedEventMapper = new TaskCompletedEventMapper();

  @Test
  void map_mapsId() {
    var taskId = TaskId.create();
    var event = TaskCompletedEvent.of(taskId);

    var result = taskCompletedEventMapper.map(event);

    assertThat(result.getTaskId()).isEqualTo(taskId.toString());
  }

  @Test
  void map_taskCompleted_mapsEventId() {
    var event = TaskCompletedEvent.of(TaskId.create());

    var result = taskCompletedEventMapper.map(event);

    assertThat(result.getEventId()).isEqualTo(event.getEventId().toString());
  }
}
