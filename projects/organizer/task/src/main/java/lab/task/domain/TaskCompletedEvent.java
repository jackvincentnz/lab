package lab.task.domain;

import lab.libs.ddd.domain.DomainEvent;

public final class TaskCompletedEvent extends DomainEvent<TaskId> {

  public static TaskCompletedEvent of(TaskId id) {
    return new TaskCompletedEvent(id);
  }

  private TaskCompletedEvent(TaskId taskId) {
    super(taskId);
  }
}
