package lab.mops.ai.application.chat.completions;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class AssistantMessage implements Message {

  private final String content;

  private final Collection<ToolCall> toolCalls;

  private AssistantMessage(String content, Collection<ToolCall> toolCalls) {
    this.content = content;
    this.toolCalls = Collections.unmodifiableCollection(toolCalls);
  }

  @Override
  public MessageRole getRole() {
    return MessageRole.ASSISTANT;
  }

  public Collection<ToolCall> getToolCalls() {
    return toolCalls;
  }

  public boolean hasToolCalls() {
    return !toolCalls.isEmpty();
  }

  public Optional<String> getContent() {
    return Optional.ofNullable(content);
  }

  public static AssistantMessage of(String content) {
    return new AssistantMessage(content, List.of());
  }

  public static AssistantMessage of(Collection<ToolCall> toolCalls) {
    return new AssistantMessage(null, toolCalls);
  }

  public static AssistantMessage of(String content, Collection<ToolCall> toolCalls) {
    return new AssistantMessage(content, toolCalls);
  }
}
