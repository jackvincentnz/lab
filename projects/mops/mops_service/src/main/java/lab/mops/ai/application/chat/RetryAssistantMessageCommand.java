package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.MessageId;

public record RetryAssistantMessageCommand(ChatId chatId, MessageId messageId) {}
