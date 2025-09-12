package lab.mops.ai.api.gql.chat;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;
import static lab.mops.common.api.gql.ResponseMessage.OK_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.mops.ai.application.chat.AddUserMessageCommand;
import lab.mops.ai.application.chat.ChatCommandService;
import lab.mops.ai.application.chat.StartChatCommand;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.api.gql.types.AddUserMessageInput;
import lab.mops.api.gql.types.AddUserMessageResponse;
import lab.mops.api.gql.types.StartChatInput;
import lab.mops.api.gql.types.StartChatResponse;

@DgsComponent
public class ChatMutation {
  private final ChatCommandService chatCommandService;

  private final ChatMapper chatMapper;

  public ChatMutation(ChatCommandService chatCommandService, ChatMapper chatMapper) {
    this.chatCommandService = chatCommandService;
    this.chatMapper = chatMapper;
  }

  @DgsMutation
  public StartChatResponse startChat(@InputArgument("input") StartChatInput input) {
    var command = new StartChatCommand(input.getContent());

    var chat = chatCommandService.startChat(command);

    return StartChatResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .chat(chatMapper.map(chat))
        .build();
  }

  @DgsMutation
  public AddUserMessageResponse addUserMessage(@InputArgument("input") AddUserMessageInput input) {
    var command =
        new AddUserMessageCommand(ChatId.fromString(input.getChatId()), input.getContent());

    var chat = chatCommandService.addUserMessage(command);

    return AddUserMessageResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
        .chat(chatMapper.map(chat))
        .build();
  }
}
