package nz.geek.jack.example.infrastructure.web.gql.task.data;

import nz.geek.jack.example.application.task.data.MarkTaskCompletedCommand;
import nz.geek.jack.example.domain.TaskId;

public record MarkTaskCompletedInput(String id) implements MarkTaskCompletedCommand {
  @Override
  public TaskId taskId() {
    return TaskId.fromString(id);
  }
}
