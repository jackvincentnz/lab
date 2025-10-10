package lab.mops.ai.domain.chat;

import java.util.List;

public record PendingToolCallsAddedEvent(ChatId chatId, List<ToolCall> toolCalls) {}
