package nz.geek.jack.task.domain;

import java.util.Objects;
import java.util.UUID;

public final class TaskId {

  private final UUID id;

  private TaskId() {
    this(UUID.randomUUID());
  }

  private TaskId(UUID id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return id.toString();
  }

  public static TaskId create() {
    return new TaskId();
  }

  public static TaskId fromString(String id) {
    return new TaskId(UUID.fromString(id));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaskId taskId = (TaskId) o;
    return id.equals(taskId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
