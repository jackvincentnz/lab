package lab.mops.ai.domain.chat;

public interface PendingAssistantMessageAddedEvent {
  ChatId chatId();

  MessageId pendingAssistantMessageId();
}
