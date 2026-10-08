package lab.mops.ai.infrastructure.outbox;

import java.util.concurrent.atomic.AtomicReference;
import lab.mops.ai.application.chat.ChatEventHandler;
import lab.mops.ai.domain.chat.ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class ChatOutboxWorker {
  private static final Logger LOG = LoggerFactory.getLogger(ChatOutboxWorker.class);
  static final int MAX_ATTEMPTS = 5;
  private final ChatOutbox outbox;
  private final ChatEventHandler handler;
  private final ChatRepository chats;
  private final TransactionTemplate transaction;

  public ChatOutboxWorker(
      ChatOutbox outbox,
      ChatEventHandler handler,
      ChatRepository chats,
      PlatformTransactionManager transactionManager) {
    this.outbox = outbox;
    this.handler = handler;
    this.chats = chats;
    this.transaction = new TransactionTemplate(transactionManager);
  }

  public boolean processNext() {
    var claimed = new AtomicReference<ChatOutbox.Work>();
    try {
      return Boolean.TRUE.equals(
          transaction.execute(
              status -> {
                var next = outbox.claim();
                if (next.isEmpty()) {
                  return false;
                }
                var work = next.get();
                claimed.set(work);
                if (work.isCompletion()) {
                  handler.onPendingAssistantMessageAdded(work);
                } else {
                  handler.onToolCallApproved(work.toolEvent());
                }
                outbox.complete(work);
                return true;
              }));
    } catch (RuntimeException failure) {
      var work = claimed.get();
      if (work == null) {
        throw failure;
      }
      LOG.warn("Chat work [{}] failed ({})", work.messageId(), failure.getClass().getSimpleName());
      // A repository failure can mark the entire transaction rollback-only. Retry metadata
      // therefore belongs in a fresh transaction, after all local tool effects roll back.
      transaction.executeWithoutResult(status -> outbox.find(work).ifPresent(this::retryOrFail));
      return true;
    }
  }

  private void retryOrFail(ChatOutbox.Work work) {
    if (work.attempts() + 1 < MAX_ATTEMPTS) {
      outbox.retry(work);
      return;
    }
    var chat = chats.getById(work.chatId());
    if (chat.getMessages().stream()
        .anyMatch(
            m ->
                m.getId().equals(work.messageId())
                    && (work.isCompletion() ? m.isPending() : m.isCompleted()))) {
      chat.failMessage(
          work.messageId(),
          "The assistant could not finish after several attempts. Please retry the response.");
      chats.save(chat);
    }
    outbox.complete(work);
  }
}
