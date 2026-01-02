package lab.mops.ai.domain.chat;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.ExternalId;

public final class ToolCallId extends ExternalId {

  private ToolCallId(String id) {
    super(id);
  }

  public static ToolCallId of(String id) {
    return new ToolCallId(id);
  }

  public static ToolCallId create() {
    return new ToolCallId(UUID.randomUUID().toString());
  }
}
