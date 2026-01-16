package lab.mops.ai.domain.chat;

import java.util.Objects;
import org.springframework.data.annotation.Id;

public final class ToolCall {

  @Id private final ToolCallId id;

  private final String name;

  private final String arguments;

  private ToolCallStatus status;

  private String result;

  private ToolCall(
      ToolCallId id, String name, String arguments, ToolCallStatus status, String result) {
    this.id = id;
    this.name = name;
    this.arguments = arguments;
    this.status = status;
    this.result = result;
  }

  void approve() {
    this.status = ToolCallStatus.APPROVED;
  }

  void recordResult(String result) {
    Objects.requireNonNull(result, "result must not be null");
    this.result = result;
  }

  void reject() {
    this.status = ToolCallStatus.REJECTED;
  }

  public ToolCallId id() {
    return id;
  }

  public String name() {
    return name;
  }

  public String arguments() {
    return arguments;
  }

  public ToolCallStatus status() {
    return status;
  }

  public String result() {
    return result;
  }

  public static ToolCall of(ToolCallId id, String name, String arguments, ToolCallStatus status) {
    return new ToolCall(id, name, arguments, status, null);
  }
}
