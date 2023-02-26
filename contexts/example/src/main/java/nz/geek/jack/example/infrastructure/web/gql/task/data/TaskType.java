package nz.geek.jack.example.infrastructure.web.gql.task.data;

import nz.geek.jack.example.domain.Task;

public record TaskType(String id, String title, boolean isCompleted) {

  public static TaskType from(Task task) {
    return new TaskType(task.getId().toString(), task.getTitle(), task.isCompleted());
  }
}
