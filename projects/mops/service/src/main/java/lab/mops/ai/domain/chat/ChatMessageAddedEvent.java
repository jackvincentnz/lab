package lab.mops.ai.domain.chat;

import java.time.Instant;

public record ChatMessageAddedEvent(
    ChatId chatId,
    MessageType type,
    String content,
    Instant timestamp,
    MessageId pendingAssistantMessageId)
    implements PendingAssistantMessageAddedEvent {}
