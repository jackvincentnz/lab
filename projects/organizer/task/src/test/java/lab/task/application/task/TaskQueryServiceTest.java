package lab.task.application.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.task.domain.Task;
import lab.task.domain.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskQueryServiceTest {

  @Mock TaskRepository taskRepository;

  @InjectMocks TaskQueryService taskQueryService;

  @Test
  void getAllTasks_returnsTasksFromRepository() {
    var tasks = List.of(Task.addTask("A task"), Task.addTask("Another task"));
    when(taskRepository.getAll()).thenReturn(tasks);

    var result = taskQueryService.getAllTasks();

    assertThat(result).isEqualTo(tasks);
  }

  @Test
  void getTask_returnsTaskFromRepository() {
    var task = Task.addTask("A task");
    when(taskRepository.get(task.getId())).thenReturn(task);

    var result = taskQueryService.getTask(task.getId());

    assertThat(result).isEqualTo(task);
  }
}
