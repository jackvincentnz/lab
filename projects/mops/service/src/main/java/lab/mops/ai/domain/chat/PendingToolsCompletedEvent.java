package lab.mops.ai.domain.chat;

public record PendingToolsCompletedEvent(ChatId chatId, MessageId pendingAssistantMessageId)
    implements PendingAssistantMessageAddedEvent {}
