package lab.mops.ai.domain.chat;

import java.time.Instant;

public record ChatStartedEvent(
    ChatId chatId, Instant createdAt, String content, MessageId pendingAssistantMessageId) {}
