package lab.mops.ai.application.chat.completions;

public record UserMessage(String content) implements Message {

  @Override
  public MessageRole getRole() {
    return MessageRole.USER;
  }
}
