package nz.geek.jack.task.domain;

import nz.geek.jack.libs.ddd.domain.DomainEvent;

public final class TaskCompletedEvent extends DomainEvent {

  private final TaskId taskId;

  public static TaskCompletedEvent of(TaskId id) {
    return new TaskCompletedEvent(id);
  }

  private TaskCompletedEvent(TaskId taskId) {
    this.taskId = taskId;
  }

  public TaskId getTaskId() {
    return taskId;
  }
}
