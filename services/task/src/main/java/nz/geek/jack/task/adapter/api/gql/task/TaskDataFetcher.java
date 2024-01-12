package nz.geek.jack.task.adapter.api.gql.task;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import nz.geek.jack.task.adapter.gql.schema.types.Task;
import nz.geek.jack.task.application.task.TaskQueryService;
import nz.geek.jack.task.domain.TaskId;

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
        .sorted(Comparator.comparing(nz.geek.jack.task.domain.Task::getCreatedAt))
        .map(taskMapper::map)
        .collect(Collectors.toList());
  }

  @DgsQuery
  public Task task(String id) {
    // TODO: error handing (not found, id format, id length)
    var task = taskQueryService.getTask(TaskId.fromString(id));

    return taskMapper.map(task);
  }
}
