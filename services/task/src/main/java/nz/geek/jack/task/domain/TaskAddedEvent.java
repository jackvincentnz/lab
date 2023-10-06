package nz.geek.jack.task.domain;

import nz.geek.jack.libs.domain.DomainEvent;

public final class TaskAddedEvent extends DomainEvent {

  private final TaskId taskId;

  private final String title;

  public static TaskAddedEvent of(TaskId taskId, String title) {
    return new TaskAddedEvent(taskId, title);
  }

  private TaskAddedEvent(TaskId taskId, String title) {
    this.taskId = taskId;
    this.title = title;
  }

  public TaskId getTaskId() {
    return taskId;
  }

  public String getTitle() {
    return title;
  }
}
