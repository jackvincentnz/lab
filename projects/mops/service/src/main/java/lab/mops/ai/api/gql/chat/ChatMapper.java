package lab.mops.ai.api.gql.chat;

import lab.mops.api.gql.types.Chat;
import lab.mops.api.gql.types.ChatMessage;
import lab.mops.api.gql.types.ChatMessageStatus;
import lab.mops.api.gql.types.ChatMessageType;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

  public Chat map(lab.mops.ai.domain.chat.Chat chat) {
    return Chat.newBuilder()
        .id(chat.getId().toString())
        .messages(
            chat.getMessages().stream()
                .map(
                    message ->
                        ChatMessage.newBuilder()
                            .id(message.getId().toString())
                            .type(ChatMessageType.valueOf(message.getType().name()))
                            .status(ChatMessageStatus.valueOf(message.getStatus().name()))
                            .content(message.getContent().orElse(null))
                            .createdAt(message.getTimestamp().toString())
                            .updatedAt(message.getTimestamp().toString())
                            .build())
                .toList())
        .createdAt(chat.getCreatedAt().toString())
        .updatedAt(chat.getUpdatedAt().toString())
        .build();
  }
}
