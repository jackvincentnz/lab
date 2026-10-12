package lab.mops.ai.domain.chat;

public interface PendingAssistantMessageAddedEvent extends ChatChangedEvent {
  ChatId chatId();

  MessageId pendingAssistantMessageId();
}
