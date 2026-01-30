package lab.task.adapter.api.gql.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.task.adapter.gql.schema.types.AddTaskInput;
import lab.task.adapter.gql.schema.types.MarkTaskCompletedInput;
import lab.task.application.task.TaskCommandService;
import lab.task.application.task.TaskQueryService;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskMutationTest {

  @Mock private TaskCommandService taskCommandService;

  @Mock private TaskQueryService taskQueryService;

  @Mock private TaskMapper taskMapper;

  @InjectMocks private TaskMutation taskMutation;

  @Test
  void addTask_addsTask() {
    var input = AddTaskInput.newBuilder().title("My Task").build();

    taskMutation.addTask(input);

    verify(taskCommandService).addTask(input.getTitle());
  }

  @Test
  void addTask_mapsAndReturnsAddedTask() {
    var title = "My Task";
    var input = AddTaskInput.newBuilder().title(title).build();
    var task = Task.addTask(title);
    var gqlTask = lab.task.adapter.gql.schema.types.Task.newBuilder().build();

    when(taskCommandService.addTask(title)).thenReturn(task.getId());
    when(taskQueryService.getTask(task.getId())).thenReturn(task);
    when(taskMapper.map(task)).thenReturn(gqlTask);

    var result = taskMutation.addTask(input);

    assertThat(result).isEqualTo(gqlTask);
  }

  @Test
  void markTaskCompleted_marksTaskCompleted() {
    var taskId = TaskId.create();
    var input = MarkTaskCompletedInput.newBuilder().id(taskId.toString()).build();

    taskMutation.markTaskCompleted(input);

    verify(taskCommandService).markTaskCompleted(taskId);
  }

  @Test
  void markTaskCompleted_mapsAndReturnsCompletedTask() {
    var task = Task.addTask("My Task");
    var input = MarkTaskCompletedInput.newBuilder().id(task.getId().toString()).build();
    var gqlTask = lab.task.adapter.gql.schema.types.Task.newBuilder().build();

    when(taskQueryService.getTask(task.getId())).thenReturn(task);
    when(taskMapper.map(task)).thenReturn(gqlTask);

    var result = taskMutation.markTaskCompleted(input);

    assertThat(result).isEqualTo(gqlTask);
  }
}
