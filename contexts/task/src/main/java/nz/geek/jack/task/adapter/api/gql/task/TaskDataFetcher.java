package nz.geek.jack.task.adapter.api.gql.task;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import java.util.stream.Collectors;
import nz.geek.jack.task.adapter.gql.schema.types.Task;
import nz.geek.jack.task.application.task.TaskQueryService;

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

    return tasks.map(taskMapper::map).collect(Collectors.toList());
  }
}
