package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.ai.domain.chat.ToolCallId;

public record ApproveToolCallCommand(ChatId chatId, MessageId messageId, ToolCallId toolCallId) {}
