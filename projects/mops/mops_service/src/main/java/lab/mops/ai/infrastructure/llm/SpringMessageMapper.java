package lab.mops.ai.infrastructure.llm;

import java.util.List;
import java.util.stream.Collectors;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.application.chat.completions.UserMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
public class SpringMessageMapper {

  public List<Message> map(List<lab.mops.ai.application.chat.completions.Message> messages) {
    return messages.stream()
        .map(
            m ->
                switch (m.getRole()) {
                  case USER -> map((UserMessage) m);
                  case ASSISTANT -> map((AssistantMessage) m);
                  case TOOL -> map((ToolResultMessage) m);
                })
        .collect(Collectors.toList());
  }

  private static org.springframework.ai.chat.messages.UserMessage map(UserMessage m) {
    return new org.springframework.ai.chat.messages.UserMessage(m.content());
  }

  private static org.springframework.ai.chat.messages.AssistantMessage map(
      AssistantMessage message) {
    var toolCalls = message.getToolCalls().stream().map(SpringMessageMapper::map).toList();

    var builder =
        org.springframework.ai.chat.messages.AssistantMessage.builder().toolCalls(toolCalls);

    message.getContent().ifPresent(builder::content);

    return builder.build();
  }

  private static org.springframework.ai.chat.messages.AssistantMessage.ToolCall map(ToolCall call) {
    return new org.springframework.ai.chat.messages.AssistantMessage.ToolCall(
        call.id(), "function", call.toolName(), call.arguments());
  }

  private static org.springframework.ai.chat.messages.ToolResponseMessage map(
      ToolResultMessage message) {
    return ToolResponseMessage.builder()
        .responses(
            List.of(
                new ToolResponseMessage.ToolResponse(
                    message.toolCallId(), message.toolName(), message.content())))
        .build();
  }

  public AssistantMessage map(ChatResponse response) {
    return AssistantMessage.of(
        response.getResult().getOutput().getText(),
        response.getResult().getOutput().getToolCalls().stream()
            .map(SpringMessageMapper::map)
            .toList());
  }

  public static ToolCall map(
      org.springframework.ai.chat.messages.AssistantMessage.ToolCall toolCall) {
    return new ToolCall(toolCall.id(), toolCall.name(), toolCall.arguments());
  }
}
