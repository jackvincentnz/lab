package nz.geek.jack.task.domain;

import java.time.Instant;
import nz.geek.jack.libs.domain.Aggregate;

public final class Task extends Aggregate {

  private TaskId id;

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
    id = taskAddedEvent.getTaskId();
    title = taskAddedEvent.getTitle();
    createdAt = taskAddedEvent.getCreatedAt();
  }

  public void markCompleted() {
    apply(TaskCompletedEvent.of(id));
  }

  private void on(TaskCompletedEvent taskCompletedEvent) {
    isCompleted = true;
  }

  public TaskId getId() {
    return id;
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
