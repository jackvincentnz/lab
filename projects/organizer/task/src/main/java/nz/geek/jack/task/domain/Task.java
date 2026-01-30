package nz.geek.jack.task.domain;

import java.time.Instant;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Task extends EventSourcedAggregate<TaskId> {

  private static final Logger LOG = LoggerFactory.getLogger(Task.class);

  private String title;

  private boolean isCompleted = false;

  private Instant createdAt;

  public static Task addTask(String title) {
    return new Task(title);
  }

  private Task(String title) {
    apply(TaskAddedEvent.of(TaskId.create(), title, Instant.now()));
  }

  private void on(TaskAddedEvent taskAddedEvent) {
    id = taskAddedEvent.getAggregateId();
    title = taskAddedEvent.getTitle();
    createdAt = taskAddedEvent.getCreatedAt();
  }

  public void markCompleted() {
    if (!isCompleted) {
      apply(TaskCompletedEvent.of(id));
    } else {
      LOG.debug(String.format("Task [%s] is already completed.", id));
    }
  }

  private void on(TaskCompletedEvent taskCompletedEvent) {
    isCompleted = true;
  }

  public String getTitle() {
    return title;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public static Task of(TaskId id, String title, boolean isCompleted, Instant createdAt) {
    return new Task(id, title, isCompleted, createdAt);
  }

  private Task(TaskId id, String title, boolean isCompleted, Instant createdAt) {
    this.id = id;
    this.title = title;
    this.isCompleted = isCompleted;
    this.createdAt = createdAt;
  }
}
