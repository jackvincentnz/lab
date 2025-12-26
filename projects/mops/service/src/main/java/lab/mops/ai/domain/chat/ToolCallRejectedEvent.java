package lab.mops.ai.domain.chat;

public record ToolCallRejectedEvent(ChatId chatId, MessageId messageId, String toolCallId) {}
