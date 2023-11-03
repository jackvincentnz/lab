package nz.geek.jack.task.domain;

import java.util.UUID;
import nz.geek.jack.libs.domain.AbstractId;

public final class TaskId extends AbstractId {

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
