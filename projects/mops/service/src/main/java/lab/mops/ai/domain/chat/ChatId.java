package lab.mops.ai.domain.chat;

import java.util.UUID;
import nz.geek.jack.libs.ddd.domain.AbstractId;

public final class ChatId extends AbstractId {

  private ChatId() {
    super();
  }

  private ChatId(UUID id) {
    super(id);
  }

  public static ChatId create() {
    return new ChatId();
  }

  public static ChatId fromString(String id) {
    return new ChatId(UUID.fromString(id));
  }
}
