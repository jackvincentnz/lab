package lab.mops.ai.domain.chat;

import java.time.Instant;

public record ChatMessageEditedEvent(
    ChatId chatId,
    MessageId messageId,
    String content,
    Instant timestamp,
    MessageId pendingAssistantMessageId)
    implements PendingAssistantMessageAddedEvent {}
