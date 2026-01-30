package lab.task.adapter.api.gql.task;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.Comparator;
import java.util.List;
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

  @DgsQuery
  public Task task(String id) {
    // TODO: error handing (not found, id format, id length)
    var task = taskQueryService.getTask(TaskId.fromString(id));

    return taskMapper.map(task);
  }
}
