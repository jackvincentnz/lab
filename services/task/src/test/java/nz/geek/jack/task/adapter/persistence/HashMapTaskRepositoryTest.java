package nz.geek.jack.task.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import nz.geek.jack.task.domain.Task;
import nz.geek.jack.task.domain.TaskId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HashMapTaskRepositoryTest {

  HashMapTaskRepository hashMapTaskRepository;

  @BeforeEach
  void setup() {
    hashMapTaskRepository = new HashMapTaskRepository();
  }

  @Test
  void saveTask_storesTasksById() {
    var task = Task.addTask("My task");

    hashMapTaskRepository.saveTask(task);

    var retrieved = hashMapTaskRepository.getTask(task.getId());

    assertThat(retrieved).isEqualTo(task);
  }

  @Test
  void getAllTasks_retrievesStoredTasks() {
    var task1 = Task.addTask("task1");
    var task2 = Task.addTask("task2");
    var task3 = Task.addTask("task3");

    hashMapTaskRepository.saveTask(task1);
    hashMapTaskRepository.saveTask(task2);
    hashMapTaskRepository.saveTask(task3);

    var tasks = hashMapTaskRepository.getAllTasks();

    assertThat(tasks).hasSize(3);
    assertThat(tasks).containsAll(List.of(task1, task2, task3));
  }

  @Test
  void getTask_throwsForMissingTask() {
    assertThrows(RuntimeException.class, () -> hashMapTaskRepository.getTask(TaskId.create()));
  }
}
