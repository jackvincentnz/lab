package nz.geek.jack.example.application.task.data;

import nz.geek.jack.example.domain.TaskId;

public final class DefaultTaskQuery implements TaskQuery {

  private final TaskId taskId;

  private DefaultTaskQuery(TaskId taskId) {
    this.taskId = taskId;
  }

  @Override
  public TaskId taskId() {
    return taskId;
  }

  public static DefaultTaskQuery of(String id) {
    var taskId = TaskId.fromString(id);
    return new DefaultTaskQuery(taskId);
  }

  public static DefaultTaskQuery of(TaskId taskId) {
    return new DefaultTaskQuery(taskId);
  }
}
