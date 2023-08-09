package nz.geek.jack.autojournal.domain;

public final class TaskCompletedEntry {

  private final String message;

  public static TaskCompletedEntry from(String title) {
    return new TaskCompletedEntry(title);
  }

  private TaskCompletedEntry(String title) {
    this.message = String.format("Completed: %s", title);
  }

  public String getMessage() {
    return message;
  }
}
