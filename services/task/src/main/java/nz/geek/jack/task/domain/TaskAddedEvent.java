package nz.geek.jack.task.domain;

import java.time.Instant;
import nz.geek.jack.libs.domain.DomainEvent;

public final class TaskAddedEvent extends DomainEvent {

  private final TaskId taskId;

  private final String title;

  private final Instant createdAt;

  public static TaskAddedEvent of(TaskId taskId, String title, Instant createdAt) {
    return new TaskAddedEvent(taskId, title, createdAt);
  }

  private TaskAddedEvent(TaskId taskId, String title, Instant createdAt) {
    this.taskId = taskId;
    this.title = title;
    this.createdAt = createdAt;
  }

  public TaskId getTaskId() {
    return taskId;
  }

  public String getTitle() {
    return title;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
