package lab.mops.ai.application.chat;

import java.util.stream.Collectors;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

  public Message map(lab.mops.ai.domain.chat.Message message) {
    return switch (message.getType()) {
      case USER -> new UserMessage(message.getContent().orElseThrow());
      case ASSISTANT ->
          message
              .getContent()
              .<Message>map(AssistantMessage::of)
              .orElseGet(
                  () ->
                      AssistantMessage.of(
                          message.getToolCalls().stream()
                              .map(MessageMapper::mapToolCall)
                              .collect(Collectors.toList())));
    };
  }

  private static ToolCall mapToolCall(lab.mops.ai.domain.chat.ToolCall toolCall) {
    return new ToolCall(toolCall.id().toString(), toolCall.name(), toolCall.arguments());
  }
}
