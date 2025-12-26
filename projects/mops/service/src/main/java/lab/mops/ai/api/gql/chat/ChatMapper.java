package lab.mops.ai.api.gql.chat;

import lab.mops.api.gql.types.Chat;
import lab.mops.api.gql.types.ChatMessage;
import lab.mops.api.gql.types.ChatMessageStatus;
import lab.mops.api.gql.types.ChatMessageType;
import lab.mops.api.gql.types.ToolCall;
import lab.mops.api.gql.types.ToolCallStatus;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

  public Chat map(lab.mops.ai.domain.chat.Chat chat) {
    return Chat.newBuilder()
        .id(chat.getId().toString())
        .messages(chat.getMessages().stream().map(ChatMapper::map).toList())
        .createdAt(chat.getCreatedAt().toString())
        .updatedAt(chat.getUpdatedAt().toString())
        .build();
  }

  private static ChatMessage map(lab.mops.ai.domain.chat.Message message) {
    return ChatMessage.newBuilder()
        .id(message.getId().toString())
        .type(ChatMessageType.valueOf(message.getType().name()))
        .status(ChatMessageStatus.valueOf(message.getStatus().name()))
        .content(message.getContent().orElse(null))
        .toolCalls(message.getToolCalls().stream().map(ChatMapper::map).toList())
        .createdAt(message.getTimestamp().toString())
        .updatedAt(message.getTimestamp().toString())
        .build();
  }

  private static ToolCall map(lab.mops.ai.domain.chat.ToolCall toolCall) {
    return ToolCall.newBuilder()
        .id(toolCall.id().toString())
        .name(toolCall.name())
        .arguments(toolCall.arguments())
        .status(ToolCallStatus.valueOf(toolCall.status().name()))
        .build();
  }
}
