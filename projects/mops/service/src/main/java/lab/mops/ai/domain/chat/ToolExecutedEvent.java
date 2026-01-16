package lab.mops.ai.domain.chat;

public record ToolExecutedEvent(
    ChatId chatId, MessageId messageId, ToolCallId toolCallId, String result) {}
