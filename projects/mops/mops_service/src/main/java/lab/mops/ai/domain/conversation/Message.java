package lab.mops.ai.domain.conversation;

import java.time.Instant;
import java.util.Objects;

public class Message {

  private final MessageType type;

  private final String content;

  private final Instant timestamp;

  private Message(MessageType type, String content, Instant timestamp) {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(content, "content must not be null");
    Objects.requireNonNull(timestamp, "timestamp must not be null");

    this.type = type;
    this.content = content;
    this.timestamp = timestamp;
  }

  public MessageType getType() {
    return type;
  }

  public String getContent() {
    return content;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public static Message create(MessageType type, String content) {
    return new Message(type, content, Instant.now());
  }
}
