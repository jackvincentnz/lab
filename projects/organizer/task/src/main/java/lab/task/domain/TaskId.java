package lab.task.domain;

import java.util.UUID;
import lab.libs.ddd.domain.InternalId;

public final class TaskId extends InternalId {

  private TaskId() {
    super();
  }

  private TaskId(UUID id) {
    super(id);
  }

  public static TaskId create() {
    return new TaskId();
  }

  public static TaskId fromString(String id) {
    return new TaskId(UUID.fromString(id));
  }
}
