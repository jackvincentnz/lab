package lab.mops.ai.domain.chat;

import java.time.Instant;

public record ChatMessageCancelledEvent(ChatId id, MessageId messageId, Instant timestamp) {}
