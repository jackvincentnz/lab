package lab.mops.ai.domain.chat;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import nz.geek.jack.libs.ddd.domain.NotFoundException;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("CHAT_MESSAGE")
public class Message {

  @Id private final MessageId id;

  private final MessageType type;

  private final Set<ToolCall> toolCalls;

  private Instant timestamp;

  private MessageStatus status;

  private String content;

  private Message(
      MessageId id,
      MessageType type,
      Instant timestamp,
      MessageStatus status,
      String content,
      Set<ToolCall> toolCalls) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    Objects.requireNonNull(type, "status must not be null");
    Objects.requireNonNull(toolCalls, "toolCalls must not be null");
    this.id = id;
    this.type = type;
    this.timestamp = timestamp;
    this.status = status;
    this.content = content;
    this.toolCalls = toolCalls;
  }

  void edit(String content) {
    Objects.requireNonNull(content, "content must not be null");
    this.content = content;
    this.timestamp = Instant.now();
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

  public boolean isPending() {
    return status == MessageStatus.PENDING;
  }

  public boolean isCompleted() {
    return status == MessageStatus.COMPLETED;
  }

  public boolean isCancelled() {
    return status == MessageStatus.CANCELLED;
  }

  public MessageStatus getStatus() {
    return status;
  }

  void addPendingToolCalls(List<ToolCall> toolCalls) {
    Objects.requireNonNull(toolCalls, "toolCalls must not be null");

    if (toolCalls.stream().anyMatch(Objects::isNull)) {
      throw new NullPointerException("toolCalls must not contain null values");
    }

    this.toolCalls.addAll(toolCalls);
    updateStatus(MessageStatus.COMPLETED);
  }

  void approveToolCall(ToolCallId toolCallId) {
    Objects.requireNonNull(toolCallId, "toolCallId must not be null");
    var toolCall = getToolCall(toolCallId);
    toolCall.approve();
  }

  void recordToolResult(ToolCallId toolCallId, String result) {
    Objects.requireNonNull(toolCallId, "toolCallId must not be null");
    Objects.requireNonNull(result, "result must not be null");

    var toolCall = getToolCall(toolCallId);

    toolCall.recordResult(result);
  }

  void rejectToolCall(ToolCallId toolCallId) {
    Objects.requireNonNull(toolCallId, "toolCallId must not be null");
    var toolCall = getToolCall(toolCallId);
    toolCall.reject();
  }

  private ToolCall getToolCall(ToolCallId toolCallId) {
    return toolCalls.stream()
        .filter(t -> t.id().equals(toolCallId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(toolCallId));
  }

  public List<ToolCall> getToolCalls() {
    return List.copyOf(toolCalls);
  }

  void complete(String content) {
    Objects.requireNonNull(content, "content must not be null");
    updateStatus(MessageStatus.COMPLETED);
    this.content = content;
  }

  void cancel() {
    updateStatus(MessageStatus.CANCELLED);
  }

  private void updateStatus(MessageStatus status) {
    this.status = status;
    this.timestamp = Instant.now();
  }

  static Message userMessage(String content) {
    Objects.requireNonNull(content, "content must not be null");

    return new Message(
        MessageId.create(),
        MessageType.USER,
        Instant.now(),
        MessageStatus.COMPLETED,
        content,
        new HashSet<>());
  }

  public static Message assistantMessage() {
    return new Message(
        MessageId.create(),
        MessageType.ASSISTANT,
        Instant.now(),
        MessageStatus.PENDING,
        null,
        new HashSet<>());
  }
}
