package lab.mops.ai.application.chat;

import java.util.ArrayList;
import java.util.List;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.domain.chat.Chat;
import org.springframework.stereotype.Component;

@Component
public class ChatContextBuilder {

  private final MessageMapper messageMapper;

  public ChatContextBuilder(MessageMapper messageMapper) {
    this.messageMapper = messageMapper;
  }

  public List<Message> buildHistory(Chat chat) {
    var conversationHistory = new ArrayList<Message>();

    chat.getMessages().stream()
        .filter(lab.mops.ai.domain.chat.Message::isCompleted)
        .forEach(
            m -> {
              var mapped = messageMapper.map(m);
              conversationHistory.add(mapped);
              if (m.getType() == lab.mops.ai.domain.chat.MessageType.ASSISTANT) {
                m.getToolCalls().stream()
                    .filter(tc -> tc.result() != null)
                    .forEach(
                        tc ->
                            conversationHistory.add(
                                new ToolResultMessage(tc.result(), tc.id().toString(), tc.name())));
              }
            });

    return conversationHistory;
  }
}
