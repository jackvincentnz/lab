package lab.mops.ai.domain.chat;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.relational.core.mapping.Table;

@Table("CHAT_MESSAGE")
public class Message {

  private final MessageId id;

  private final MessageType type;

  private Instant timestamp;

  private MessageStatus status;

  private String content;

  private Message(
      MessageId id, MessageType type, Instant timestamp, MessageStatus status, String content) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    Objects.requireNonNull(type, "status must not be null");

    this.id = id;
    this.type = type;
    this.timestamp = timestamp;
    this.status = status;
    this.content = content;
  }

  public MessageId getId() {
    return id;
  }

  public MessageType getType() {
    return type;
  }

  public Optional<String> getContent() {
    return Optional.ofNullable(content);
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public MessageStatus getStatus() {
    return status;
  }

  void complete(String content) {
    this.status = MessageStatus.COMPLETED;
    this.content = content;
    this.timestamp = Instant.now();
  }

  static Message userMessage(String userPrompt) {
    Objects.requireNonNull(userPrompt, "userPrompt must not be null");

    return new Message(
        MessageId.create(), MessageType.USER, Instant.now(), MessageStatus.COMPLETED, userPrompt);
  }

  static Message assistantMessage() {
    return new Message(
        MessageId.create(), MessageType.ASSISTANT, Instant.now(), MessageStatus.PENDING, null);
  }
}
