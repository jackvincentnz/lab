package nz.geek.jack.task.infrastructure.web.gql.task.data;

import nz.geek.jack.task.domain.Task;

public record TaskType(String id, String title, boolean isCompleted) {

  public static TaskType from(Task task) {
    return new TaskType(task.getId().toString(), task.getTitle(), task.isCompleted());
  }
}
