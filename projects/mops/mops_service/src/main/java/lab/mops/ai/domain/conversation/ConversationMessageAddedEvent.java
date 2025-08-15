package lab.mops.ai.domain.conversation;

import java.time.Instant;

public record ConversationMessageAddedEvent(
    ConversationId conversationId, MessageType type, String content, Instant timestamp) {}
