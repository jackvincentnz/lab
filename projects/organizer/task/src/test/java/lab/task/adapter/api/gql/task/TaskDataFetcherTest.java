package lab.task.adapter.api.gql.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.task.application.task.TaskQueryService;
import lab.task.domain.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskDataFetcherTest {

  @Mock private TaskQueryService taskQueryService;

  @Mock private TaskMapper taskMapper;

  @InjectMocks private TaskDataFetcher taskDataFetcher;

  @Test
  void allTasks_adaptsTasksToGqlModel() {
    var domainTask = Task.addTask("My Task");
    when(taskQueryService.getAllTasks()).thenReturn(List.of(domainTask));

    var gqlTask = lab.task.adapter.gql.schema.types.Task.newBuilder().build();
    when(taskMapper.map(domainTask)).thenReturn(gqlTask);

    var tasks = taskDataFetcher.allTasks();

    assertThat(tasks.size()).isEqualTo(1);
    assertThat(tasks.get(0)).isSameAs(gqlTask);
  }

  @Test
  void task_adaptsTaskToGqlModel() {
    var domainTask = Task.addTask("My Task");
    when(taskQueryService.getTask(eq(domainTask.getId()))).thenReturn(domainTask);

    var gqlTask = lab.task.adapter.gql.schema.types.Task.newBuilder().build();
    when(taskMapper.map(domainTask)).thenReturn(gqlTask);

    var task = taskDataFetcher.task(domainTask.getId().toString());

    assertThat(task).isSameAs(gqlTask);
  }
}
