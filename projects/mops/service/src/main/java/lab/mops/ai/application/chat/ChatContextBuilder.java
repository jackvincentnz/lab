package lab.mops.ai.application.chat;

import java.util.ArrayList;
import java.util.List;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ToolCallStatus;
import org.springframework.stereotype.Component;

@Component
public class ChatContextBuilder {

  protected static final String REJECTED_TOOL_CALL_RESULT = "\"Rejected\"";

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
                m.getToolCalls()
                    .forEach(
                        tc -> {
                          if (tc.result() != null) {
                            conversationHistory.add(
                                new ToolResultMessage(tc.result(), tc.id().toString(), tc.name()));
                          } else if (tc.status().equals(ToolCallStatus.REJECTED)) {
                            conversationHistory.add(
                                new ToolResultMessage(
                                    REJECTED_TOOL_CALL_RESULT, tc.id().toString(), tc.name()));
                          }
                        });
              }
            });

    return conversationHistory;
  }
}
