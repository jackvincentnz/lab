package nz.geek.jack.task.adapter.web.gql.task;

import java.util.Collection;
import java.util.stream.Collectors;
import nz.geek.jack.task.adapter.web.gql.task.data.AddTaskInput;
import nz.geek.jack.task.adapter.web.gql.task.data.MarkTaskCompletedInput;
import nz.geek.jack.task.adapter.web.gql.task.data.TaskType;
import nz.geek.jack.task.application.task.TaskCommandService;
import nz.geek.jack.task.application.task.TaskQueryService;
import nz.geek.jack.task.application.task.data.DefaultTaskQuery;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class TaskController {

  private final TaskQueryService taskQueryService;

  private final TaskCommandService taskCommandService;

  public TaskController(TaskQueryService taskQueryService, TaskCommandService taskCommandService) {
    this.taskQueryService = taskQueryService;
    this.taskCommandService = taskCommandService;
  }

  @QueryMapping
  public Collection<TaskType> allTasks() {
    return taskQueryService.getAllTasks().stream().map(TaskType::from).collect(Collectors.toList());
  }

  @MutationMapping
  public TaskType addTask(@Argument AddTaskInput input) {
    var response = taskCommandService.addTask(input);

    var taskQuery = DefaultTaskQuery.of(response.taskId());
    var task = taskQueryService.getTask(taskQuery);

    return TaskType.from(task);
  }

  @MutationMapping
  public TaskType markTaskCompleted(@Argument MarkTaskCompletedInput input) {
    var taskId = input.taskId();

    taskCommandService.markTaskCompleted(input);

    var taskQuery = DefaultTaskQuery.of(taskId);
    var task = taskQueryService.getTask(taskQuery);

    return TaskType.from(task);
  }
}
