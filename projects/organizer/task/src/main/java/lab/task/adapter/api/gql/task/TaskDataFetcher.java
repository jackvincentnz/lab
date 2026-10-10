package lab.task.adapter.api.gql.task;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.exceptions.DgsBadRequestException;
import com.netflix.graphql.dgs.exceptions.DgsEntityNotFoundException;
import java.util.Comparator;
import java.util.List;
import lab.libs.ddd.domain.NotFoundException;
import lab.task.adapter.gql.schema.types.Task;
import lab.task.application.task.TaskQueryService;
import lab.task.domain.TaskId;

@DgsComponent
public class TaskDataFetcher {

  private final TaskQueryService taskQueryService;

  private final TaskMapper taskMapper;

  public TaskDataFetcher(TaskQueryService taskQueryService, TaskMapper taskMapper) {
    this.taskQueryService = taskQueryService;
    this.taskMapper = taskMapper;
  }

  @DgsQuery
  public List<Task> allTasks() {
    var tasks = taskQueryService.getAllTasks().stream();

    return tasks
        .sorted(Comparator.comparing(lab.task.domain.Task::getCreatedAt))
        .map(taskMapper::map)
        .toList();
  }

  // DGS reports its own exception types as BAD_REQUEST or NOT_FOUND; any other exception is
  // reported as INTERNAL.
  @DgsQuery
  public Task task(String id) {
    var taskId = parseTaskId(id);

    try {
      return taskMapper.map(taskQueryService.getTask(taskId));
    } catch (NotFoundException e) {
      throw new DgsEntityNotFoundException(e.getMessage());
    }
  }

  private static TaskId parseTaskId(String id) {
    try {
      return TaskId.fromString(id);
    } catch (IllegalArgumentException e) {
      throw new DgsBadRequestException(String.format("Invalid task id [%s]", id));
    }
  }
}
