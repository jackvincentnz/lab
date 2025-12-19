package lab.mops.ai.domain.chat;

import java.time.Instant;

public record AssistantMessageRetriedEvent(
    ChatId chatId,
    MessageId retriedMessageId,
    MessageId pendingAssistantMessageId,
    Instant timestamp)
    implements PendingAssistantMessageAddedEvent {}
