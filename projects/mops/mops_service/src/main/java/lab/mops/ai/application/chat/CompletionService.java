package lab.mops.ai.application.chat;

import java.util.List;
import lab.mops.ai.domain.chat.Message;

public interface CompletionService {

  String getResponse(List<Message> messages);
}
