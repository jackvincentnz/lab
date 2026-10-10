package lab.mops.ai.domain.chat;

public record ChatMessageFailedEvent(ChatId chatId, MessageId messageId, String reason) {}
