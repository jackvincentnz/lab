package lab.task.adapter.api.gql.task;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.task.adapter.gql.schema.types.AddTaskInput;
import lab.task.adapter.gql.schema.types.MarkTaskCompletedInput;
import lab.task.adapter.gql.schema.types.Task;
import lab.task.application.task.TaskCommandService;
import lab.task.application.task.TaskQueryService;
import lab.task.domain.TaskId;

@DgsComponent
public class TaskMutation {

  private final TaskCommandService taskCommandService;

  private final TaskQueryService taskQueryService;

  private final TaskMapper taskMapper;

  public TaskMutation(
      TaskCommandService taskCommandService,
      TaskQueryService taskQueryService,
      TaskMapper taskMapper) {
    this.taskCommandService = taskCommandService;
    this.taskQueryService = taskQueryService;
    this.taskMapper = taskMapper;
  }

  @DgsMutation
  public Task addTask(@InputArgument("input") AddTaskInput input) {
    var taskId = taskCommandService.addTask(input.getTitle());

    var entry = taskQueryService.getTask(taskId);

    return taskMapper.map(entry);
  }

  @DgsMutation
  public Task markTaskCompleted(@InputArgument("input") MarkTaskCompletedInput input) {
    var taskId = TaskId.fromString(input.getId());

    taskCommandService.markTaskCompleted(taskId);

    var task = taskQueryService.getTask(taskId);

    return taskMapper.map(task);
  }
}
