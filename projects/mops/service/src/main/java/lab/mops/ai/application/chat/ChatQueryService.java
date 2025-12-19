package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatRepository;
import org.springframework.stereotype.Component;

@Component
public class ChatQueryService {

  private final ChatRepository chatRepository;

  public ChatQueryService(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;
  }

  public Chat getById(ChatId chatId) {
    return chatRepository.getById(chatId);
  }
}
