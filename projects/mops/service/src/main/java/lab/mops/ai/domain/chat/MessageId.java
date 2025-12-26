package lab.mops.ai.domain.chat;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.InternalId;

public final class MessageId extends InternalId {

  private MessageId() {
    super();
  }

  private MessageId(UUID id) {
    super(id);
  }

  public static MessageId create() {
    return new MessageId();
  }

  public static MessageId fromString(String id) {
    return new MessageId(UUID.fromString(id));
  }
}
