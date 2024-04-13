package nz.geek.jack.plan.domain.activity;

import java.util.UUID;
import nz.geek.jack.libs.domain.AbstractId;

public final class ActivityId extends AbstractId {

  private ActivityId() {
    super();
  }

  private ActivityId(UUID id) {
    super(id);
  }

  public static ActivityId create() {
    return new ActivityId();
  }

  public static ActivityId fromString(String id) {
    return new ActivityId(UUID.fromString(id));
  }
}
