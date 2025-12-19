package lab.mops.ai.application.chat.completions;

public record ToolResultMessage(String content, String toolCallId, String toolName)
    implements Message {

  @Override
  public MessageRole getRole() {
    return MessageRole.TOOL;
  }
}
