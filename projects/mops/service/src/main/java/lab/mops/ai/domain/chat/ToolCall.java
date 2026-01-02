package lab.mops.ai.domain.chat;

import org.springframework.data.annotation.Id;

public final class ToolCall {

  @Id private final ToolCallId id;

  private final String name;

  private final String arguments;

  private final ToolCallStatus status;

  private ToolCall(ToolCallId id, String name, String arguments, ToolCallStatus status) {
    this.id = id;
    this.name = name;
    this.arguments = arguments;
    this.status = status;
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

  public static ToolCall of(ToolCallId id, String name, String arguments, ToolCallStatus status) {
    return new ToolCall(id, name, arguments, status);
  }
}
