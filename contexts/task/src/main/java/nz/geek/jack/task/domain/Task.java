package nz.geek.jack.task.domain;

public final class Task {

  private static final boolean DEFAULT_IS_COMPLETED = false;

  private final TaskId id;

  private final String title;

  private boolean isCompleted = DEFAULT_IS_COMPLETED;

  private Task(TaskId id, String title) {
    this.id = id;
    this.title = title;
  }

  public TaskId getId() {
    return id;
  }

  public void markCompleted() {
    isCompleted = true;
  }

  public static Task addTask(String title) {
    return new Task(TaskId.create(), title);
  }

  public String getTitle() {
    return title;
  }

  public boolean isCompleted() {
    return isCompleted;
  }
}
