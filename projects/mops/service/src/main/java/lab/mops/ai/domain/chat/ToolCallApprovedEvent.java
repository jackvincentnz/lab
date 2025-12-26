package lab.mops.ai.domain.chat;

public record ToolCallApprovedEvent(ChatId chatId, MessageId messageId, String toolCallId) {}
