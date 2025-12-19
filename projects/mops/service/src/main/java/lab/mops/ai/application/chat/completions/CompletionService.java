package lab.mops.ai.application.chat.completions;

import java.util.List;

public interface CompletionService {
  AssistantMessage getResponse(List<Message> messages);
}
