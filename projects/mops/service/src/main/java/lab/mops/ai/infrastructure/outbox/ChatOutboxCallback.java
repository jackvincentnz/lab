package lab.mops.ai.infrastructure.outbox;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class ChatOutboxCallback implements AfterSaveCallback<Chat> {
  private final ChatOutbox outbox;

  public ChatOutboxCallback(ChatOutbox outbox) {
    this.outbox = outbox;
  }

  @Override
  public Chat onAfterSave(Chat chat) {
    // Repository event publication can occur outside its save transaction; the callback cannot.
    for (var event : chat.domainEvents()) {
      if (event instanceof PendingAssistantMessageAddedEvent pending) {
        outbox.onPendingAssistantMessageAdded(pending);
      } else if (event instanceof ToolCallApprovedEvent approved) {
        outbox.onToolCallApproved(approved);
      }
    }
    return chat;
  }
}
