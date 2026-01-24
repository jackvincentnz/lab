package lab.mops.ai.api.gql.chat;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import lab.mops.ai.application.chat.ChatQueryService;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.api.gql.types.Chat;

@DgsComponent
public class ChatDataFetcher {

  private final ChatQueryService chatQueryService;

  private final ChatMapper chatMapper;

  public ChatDataFetcher(ChatQueryService chatQueryService, ChatMapper chatMapper) {
    this.chatQueryService = chatQueryService;
    this.chatMapper = chatMapper;
  }

  @DgsQuery
  public Chat chat(String id) {
    var chat = chatQueryService.getById(ChatId.fromString(id));

    return chatMapper.map(chat);
  }

  @DgsQuery
  public List<Chat> allChats() {
    return chatQueryService.findAll().stream().map(chatMapper::map).toList();
  }
}
