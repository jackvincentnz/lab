package lab.mops.ai.api.gql.chat;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;
import static lab.mops.common.api.gql.ResponseMessage.OK_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.ai.application.chat.AddUserMessageCommand;
import lab.mops.ai.application.chat.ChatCommandService;
import lab.mops.ai.application.chat.EditUserMessageCommand;
import lab.mops.ai.application.chat.RetryAssistantMessageCommand;
import lab.mops.ai.application.chat.StartChatCommand;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.api.gql.types.AddUserMessageInput;
import lab.mops.api.gql.types.EditUserMessageInput;
import lab.mops.api.gql.types.RetryAssistantMessageInput;
import lab.mops.api.gql.types.StartChatInput;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatMutationTest extends TestBase {

  @Mock ChatCommandService chatCommandService;

  @Mock ChatMapper chatMapper;

  @InjectMocks ChatMutation chatMutation;

  @Test
  void startChat_starts() {
    var content = randomString();

    chatMutation.startChat(StartChatInput.newBuilder().content(content).build());

    verify(chatCommandService).startChat(new StartChatCommand(content));
  }

  @Test
  void startChat_mapsResponse() {
    var content = randomString();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatCommandService.startChat(new StartChatCommand(content))).thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var result = chatMutation.startChat(StartChatInput.newBuilder().content(content).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getChat()).isEqualTo(graphChat);
  }

  @Test
  void addUserMessage_adds() {
    var chatId = randomId();
    var content = randomString();

    chatMutation.addUserMessage(
        AddUserMessageInput.newBuilder().chatId(chatId).content(content).build());

    verify(chatCommandService)
        .addUserMessage(new AddUserMessageCommand(ChatId.fromString(chatId), content));
  }

  @Test
  void addUserMessage_mapsResponse() {
    var chatId = randomId();
    var content = randomString();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatCommandService.addUserMessage(
            new AddUserMessageCommand(ChatId.fromString(chatId), content)))
        .thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var result =
        chatMutation.addUserMessage(
            AddUserMessageInput.newBuilder().chatId(chatId).content(content).build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getChat()).isEqualTo(graphChat);
  }

  @Test
  void editUserMessage_edits() {
    var chatId = randomId();
    var messageId = randomId();
    var content = randomString();

    chatMutation.editUserMessage(
        EditUserMessageInput.newBuilder()
            .chatId(chatId)
            .messageId(messageId)
            .content(content)
            .build());

    verify(chatCommandService)
        .editUserMessage(
            new EditUserMessageCommand(
                ChatId.fromString(chatId), MessageId.fromString(messageId), content));
  }

  @Test
  void editUserMessage_mapsResponse() {
    var chatId = randomId();
    var messageId = randomId();
    var content = randomString();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatCommandService.editUserMessage(
            new EditUserMessageCommand(
                ChatId.fromString(chatId), MessageId.fromString(messageId), content)))
        .thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var result =
        chatMutation.editUserMessage(
            EditUserMessageInput.newBuilder()
                .chatId(chatId)
                .messageId(messageId)
                .content(content)
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getChat()).isEqualTo(graphChat);
  }

  @Test
  void retryAssistantMessage_retries() {
    var chatId = randomId();
    var messageId = randomId();

    chatMutation.retryAssistantMessage(
        RetryAssistantMessageInput.newBuilder().chatId(chatId).messageId(messageId).build());

    verify(chatCommandService)
        .retryAssistantMessage(
            new RetryAssistantMessageCommand(
                ChatId.fromString(chatId), MessageId.fromString(messageId)));
  }

  @Test
  void retryAssistantMessage_mapsResponse() {
    var chatId = randomId();
    var messageId = randomId();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatCommandService.retryAssistantMessage(
            new RetryAssistantMessageCommand(
                ChatId.fromString(chatId), MessageId.fromString(messageId))))
        .thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var result =
        chatMutation.retryAssistantMessage(
            RetryAssistantMessageInput.newBuilder().chatId(chatId).messageId(messageId).build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getChat()).isEqualTo(graphChat);
  }
}
