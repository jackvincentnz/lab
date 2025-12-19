package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.ChatId;

public record AddUserMessageCommand(ChatId chatId, String content) {}
