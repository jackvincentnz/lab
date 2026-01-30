package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.ai.domain.chat.MessageType;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatCommandServiceTest extends TestBase {

  @Mock ChatRepository chatRepository;

  @InjectMocks ChatCommandService chatCommandService;

  @Captor ArgumentCaptor<Chat> chatCaptor;

  @Test
  void startChat_startsWithContent() {
    var command = new StartChatCommand(randomString());

    chatCommandService.startChat(command);

    verify(chatRepository).save(chatCaptor.capture());
    var savedChat = chatCaptor.getValue();
    assertThat(savedChat.getMessages()).hasSize(2);
    assertThat(savedChat.getMessages().get(0).getType()).isEqualTo(MessageType.USER);
    assertThat(savedChat.getMessages().get(0).getContent().orElseThrow())
        .isEqualTo(command.content());
  }

  @Test
  void startChat_returnsSavedChat() {
    var command = new StartChatCommand(randomString());
    var savedChat = mock(Chat.class);

    when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

    var result = chatCommandService.startChat(command);

    assertThat(result).isEqualTo(savedChat);
  }

  @Test
  void addUserMessage_savesChatWithContent() {
    var chat = mock(Chat.class);
    var command = new AddUserMessageCommand(chat.getId(), randomString());

    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatCommandService.addUserMessage(command);

    verify(chat).addUserMessage(command.content());
    verify(chatRepository).save(chat);
  }

  @Test
  void addUserMessage_returnsSavedChat() {
    var chat = mock(Chat.class);
    var command = new AddUserMessageCommand(chat.getId(), randomString());
    var savedChat = mock(Chat.class);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(chatRepository.save(chat)).thenReturn(savedChat);

    var result = chatCommandService.addUserMessage(command);

    assertThat(result).isEqualTo(savedChat);
  }

  @Test
  void editUserMessage_savesChat() {
    var chat = mock(Chat.class);
    var messageId = mock(MessageId.class);
    var command = new EditUserMessageCommand(chat.getId(), messageId, randomString());

    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatCommandService.editUserMessage(command);

    verify(chat).editUserMessage(messageId, command.content());
    verify(chatRepository).save(chat);
  }

  @Test
  void editUserMessage_returnsSavedChat() {
    var chat = mock(Chat.class);
    var messageId = mock(MessageId.class);
    var command = new EditUserMessageCommand(chat.getId(), messageId, randomString());
    var savedChat = mock(Chat.class);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(chatRepository.save(chat)).thenReturn(savedChat);

    var result = chatCommandService.editUserMessage(command);

    assertThat(result).isEqualTo(savedChat);
  }

  @Test
  void retryAssistantMessage_savesChat() {
    var chat = mock(Chat.class);
    var messageId = mock(MessageId.class);
    var command = new RetryAssistantMessageCommand(chat.getId(), messageId);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatCommandService.retryAssistantMessage(command);

    verify(chat).retryAssistantMessage(messageId);
    verify(chatRepository).save(chat);
  }

  @Test
  void retryAssistantMessage_returnsSavedChat() {
    var chat = mock(Chat.class);
    var messageId = mock(MessageId.class);
    var command = new RetryAssistantMessageCommand(chat.getId(), messageId);
    var savedChat = mock(Chat.class);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(chatRepository.save(chat)).thenReturn(savedChat);

    var result = chatCommandService.retryAssistantMessage(command);

    assertThat(result).isEqualTo(savedChat);
  }

  @Test
  void approveToolCall_savesChat() {
    var chat = mock(Chat.class);
    var toolCallId = mock(ToolCallId.class);
    var messageId = mock(MessageId.class);
    var command = new ApproveToolCallCommand(chat.getId(), messageId, toolCallId);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatCommandService.approveToolCall(command);

    verify(chat).approveToolCall(messageId, toolCallId);
    verify(chatRepository).save(chat);
  }

  @Test
  void approveToolCall_returnsSavedChat() {
    var chat = mock(Chat.class);
    var toolCallId = mock(ToolCallId.class);
    var messageId = mock(MessageId.class);
    var command = new ApproveToolCallCommand(chat.getId(), messageId, toolCallId);
    var savedChat = mock(Chat.class);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(chatRepository.save(chat)).thenReturn(savedChat);

    var result = chatCommandService.approveToolCall(command);

    assertThat(result).isEqualTo(savedChat);
    verify(chat).approveToolCall(messageId, toolCallId);
    verify(chatRepository).save(chat);
  }

  @Test
  void rejectToolCall_savesChat() {
    var chat = mock(Chat.class);
    var toolCallId = mock(ToolCallId.class);
    var messageId = mock(MessageId.class);
    var command = new RejectToolCallCommand(chat.getId(), messageId, toolCallId);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatCommandService.rejectToolCall(command);

    verify(chat).rejectToolCall(messageId, toolCallId);
    verify(chatRepository).save(chat);
  }

  @Test
  void rejectToolCall_returnsSavedChat() {
    var chat = mock(Chat.class);
    var toolCallId = mock(ToolCallId.class);
    var messageId = mock(MessageId.class);
    var command = new RejectToolCallCommand(chat.getId(), messageId, toolCallId);
    var savedChat = mock(Chat.class);

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(chatRepository.save(chat)).thenReturn(savedChat);

    var result = chatCommandService.rejectToolCall(command);

    assertThat(result).isEqualTo(savedChat);
    verify(chat).rejectToolCall(messageId, toolCallId);
    verify(chatRepository).save(chat);
  }
}
