package lab.task.adapter.api.gql.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
import com.netflix.graphql.dgs.exceptions.DgsEntityNotFoundException;
import java.util.List;
import lab.libs.ddd.domain.NotFoundException;
import lab.task.application.task.TaskQueryService;
import lab.task.domain.Task;
import lab.task.domain.TaskId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskDataFetcherTest extends TestBase {

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

  @Test
  void task_withUnknownId_throwsEntityNotFound() {
    var id = TaskId.fromString(randomId());
    when(taskQueryService.getTask(id)).thenThrow(new NotFoundException(id));

    assertThatThrownBy(() -> taskDataFetcher.task(id.toString()))
        .isInstanceOf(DgsEntityNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void task_withMalformedId_throwsBadRequest() {
    var id = randomString();

    assertThatThrownBy(() -> taskDataFetcher.task(id))
        .isInstanceOf(DgsBadRequestException.class)
        .hasMessageContaining(id);
    verifyNoInteractions(taskQueryService);
  }

  @Test
  void task_withOverlongId_throwsBadRequest() {
    var id = randomId() + randomString();

    assertThatThrownBy(() -> taskDataFetcher.task(id))
        .isInstanceOf(DgsBadRequestException.class)
        .hasMessageContaining(id);
    verifyNoInteractions(taskQueryService);
  }
}
