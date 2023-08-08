package nz.geek.jack.task.domain;

import nz.geek.jack.libs.domain.Aggregate;

public final class Task extends Aggregate {

  private TaskId id;

  private String title;

  private boolean isCompleted = false;

  public static Task addTask(String title) {
    return new Task(title);
  }

  private Task(String title) {
    apply(TaskAddedEvent.of(TaskId.create(), title));
  }

  private void on(TaskAddedEvent taskAddedEvent) {
    id = taskAddedEvent.getTaskId();
    title = taskAddedEvent.getTitle();
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

  public static Task of(TaskId id, String title) {
    return new Task(id, title);
  }

  private Task(TaskId id, String title) {
    this.id = id;
    this.title = title;
  }
}
