package lab.mops.ai.domain.chat;

public record ChatMessageCompletedEvent(ChatId chatId, MessageId messageId, String content) {}
