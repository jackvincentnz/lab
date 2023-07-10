package nz.geek.jack.task.infrastructure.web.gql.task.data;

import nz.geek.jack.task.application.task.data.MarkTaskCompletedCommand;
import nz.geek.jack.task.domain.TaskId;

public record MarkTaskCompletedInput(String id) implements MarkTaskCompletedCommand {
  @Override
  public TaskId taskId() {
    return TaskId.fromString(id);
  }
}
