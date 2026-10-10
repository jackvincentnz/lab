package lab.task.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lab.libs.ddd.domain.NotFoundException;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class InMemoryTaskRepositoryTest extends TestBase {

  private final InMemoryTaskRepository repository = new InMemoryTaskRepository();

  @Test
  void get_withSavedTask_returnsTask() {
    var task = Task.addTask(randomString());
    repository.save(task);

    var result = repository.get(task.getId());

    assertThat(result).isSameAs(task);
  }

  @Test
  void get_withUnknownId_throwsNotFoundException() {
    var id = TaskId.fromString(randomId());

    assertThatThrownBy(() -> repository.get(id)).isInstanceOf(NotFoundException.class);
  }
}
