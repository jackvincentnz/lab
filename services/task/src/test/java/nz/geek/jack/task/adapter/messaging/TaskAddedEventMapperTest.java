package nz.geek.jack.task.adapter.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.task.domain.TaskAddedEvent;
import nz.geek.jack.task.domain.TaskId;
import org.junit.jupiter.api.Test;

class TaskAddedEventMapperTest {

  TaskAddedEventMapper taskAddedEventMapper = new TaskAddedEventMapper();

  @Test
  void map_mapsId() {
    var taskId = TaskId.create();

    var result = taskAddedEventMapper.map(TaskAddedEvent.of(taskId, "My title"));

    assertThat(result.getTaskId()).isEqualTo(taskId.toString());
  }

  @Test
  void map_mapsTitle() {
    var title = "My title";

    var result = taskAddedEventMapper.map(TaskAddedEvent.of(TaskId.create(), title));

    assertThat(result.getTitle()).isEqualTo(title);
  }
}
