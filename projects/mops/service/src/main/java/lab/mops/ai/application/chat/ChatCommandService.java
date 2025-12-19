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
    var chat = Chat.start(command.content());

    return chatRepository.save(chat);
  }

  public Chat addUserMessage(AddUserMessageCommand command) {
    var chat = chatRepository.getById(command.chatId());

    chat.addUserMessage(command.content());

    return chatRepository.save(chat);
  }

  public Chat editUserMessage(EditUserMessageCommand command) {
    var chat = chatRepository.getById(command.chatId());

    chat.editUserMessage(command.messageId(), command.content());

    return chatRepository.save(chat);
  }

  public Chat retryAssistantMessage(RetryAssistantMessageCommand command) {
    var chat = chatRepository.getById(command.chatId());

    chat.retryAssistantMessage(command.messageId());

    return chatRepository.save(chat);
  }
}
