package lab.mops.ai.application.chat;

import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.UserMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

  public Message map(lab.mops.ai.domain.chat.Message message) {
    return switch (message.getType()) {
      case USER -> new UserMessage(message.getContent().orElseThrow());
      case ASSISTANT -> AssistantMessage.of(message.getContent().orElseThrow());
    };
  }
}
