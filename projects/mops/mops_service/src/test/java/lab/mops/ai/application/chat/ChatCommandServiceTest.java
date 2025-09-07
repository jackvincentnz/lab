package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.MessageType;
import nz.geek.jack.test.TestBase;
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
  void startChat_startsWithUserPrompt() {
    var command = new StartChatCommand(randomString());

    chatCommandService.startChat(command);

    verify(chatRepository).save(chatCaptor.capture());
    var savedChat = chatCaptor.getValue();
    assertThat(savedChat.getMessages()).hasSize(1);
    assertThat(savedChat.getMessages().get(0).getType()).isEqualTo(MessageType.USER);
    assertThat(savedChat.getMessages().get(0).getContent().orElseThrow())
        .isEqualTo(command.userPrompt());
  }

  @Test
  void startChat_returnsSavedChat() {
    var command = new StartChatCommand(randomString());
    var savedChat = mock(Chat.class);

    when(chatRepository.save(any(Chat.class))).thenReturn(savedChat);

    var result = chatCommandService.startChat(command);

    assertThat(result).isEqualTo(savedChat);
  }
}
