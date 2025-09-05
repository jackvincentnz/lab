package lab.mops.ai.application.chat;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
import org.springframework.stereotype.Component;

@Component
public class ChatCommandService {

  private final ChatRepository chatRepository;

  public ChatCommandService(ChatRepository chatRepository) {
    this.chatRepository = chatRepository;
  }

  public Chat startChat(StartChatCommand command) {
    var chat = Chat.start(command.userPrompt());

    return chatRepository.save(chat);
  }
}
