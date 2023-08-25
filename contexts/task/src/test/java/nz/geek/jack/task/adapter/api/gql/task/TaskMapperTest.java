package nz.geek.jack.task.adapter.api.gql.task;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.task.domain.Task;
import org.junit.jupiter.api.Test;

class TaskMapperTest {

  TaskMapper mapper = new TaskMapper();

  @Test
  void map_mapsId() {
    var task = Task.addTask("My Task");

    var result = mapper.map(task);

    assertThat(result.getId()).isEqualTo(task.getId().toString());
  }

  @Test
  void map_mapsTitle() {
    var task = Task.addTask("My Task");

    var result = mapper.map(task);

    assertThat(result.getTitle()).isEqualTo(task.getTitle());
  }

  @Test
  void map_mapsIsCompleted() {
    var task = Task.addTask("My Task");

    var result = mapper.map(task);

    assertThat(result.getIsCompleted()).isEqualTo(task.isCompleted());
  }
}
