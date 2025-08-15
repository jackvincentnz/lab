package lab.mops.ai.domain.conversation;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class ConversationId extends AbstractId {

  private ConversationId() {
    super();
  }

  private ConversationId(UUID id) {
    super(id);
  }

  public static ConversationId create() {
    return new ConversationId();
  }

  public static ConversationId fromString(String id) {
    return new ConversationId(UUID.fromString(id));
  }
}
