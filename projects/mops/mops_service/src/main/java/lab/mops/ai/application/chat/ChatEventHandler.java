package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.Message;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

// FIXME: there is no guarantee these will run, so an assistant message could remain PENDING
@Component
public class ChatEventHandler {

  private final ChatRepository chatRepository;

  private final CompletionService completionService;

  public ChatEventHandler(ChatRepository chatRepository, CompletionService completionService) {
    this.chatRepository = chatRepository;
    this.completionService = completionService;
  }

  @Async
  @EventListener
  public void onPendingAssistantMessageAdded(PendingAssistantMessageAddedEvent event) {
    var chat = chatRepository.getById(event.chatId());

    var response =
        completionService.getResponse(
            chat.getMessages().stream().filter(Message::isCompleted).toList());

    chat.completeMessage(event.pendingAssistantMessageId(), response);

    chatRepository.save(chat);
  }
}
