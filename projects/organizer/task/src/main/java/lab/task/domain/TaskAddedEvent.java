package lab.task.domain;

import java.time.Instant;
import lab.libs.ddd.domain.DomainEvent;

public final class TaskAddedEvent extends DomainEvent<TaskId> {

  private final String title;

  private final Instant createdAt;

  public static TaskAddedEvent of(TaskId taskId, String title, Instant createdAt) {
    return new TaskAddedEvent(taskId, title, createdAt);
  }

  private TaskAddedEvent(TaskId taskId, String title, Instant createdAt) {
    super(taskId);
    this.title = title;
    this.createdAt = createdAt;
  }

  public String getTitle() {
    return title;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
