package nz.geek.jack.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.task.domain.TaskCompletedEvent;
import nz.geek.jack.task.domain.TaskId;
import org.junit.jupiter.api.Test;

class TaskCompletedEventMapperTest {

  TaskCompletedEventMapper taskCompletedEventMapper = new TaskCompletedEventMapper();

  @Test
  void map_mapsId() {
    var taskId = TaskId.create();
    var event = TaskCompletedEvent.of(taskId);

    var result = taskCompletedEventMapper.map(event);

    assertThat(result.getTaskId()).isEqualTo(taskId.toString());
  }
}
