package lab.mops.ai.api.gql.chat;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.ai.application.chat.ChatCommandService;
import lab.mops.ai.application.chat.StartChatCommand;
import lab.mops.ai.domain.chat.Chat;
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
    var userPrompt = randomString();

    chatMutation.startChat(StartChatInput.newBuilder().userPrompt(userPrompt).build());

    verify(chatCommandService).startChat(new StartChatCommand(userPrompt));
  }

  @Test
  void startChat_mapsResponse() {
    var userPrompt = randomString();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatCommandService.startChat(new StartChatCommand(userPrompt))).thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var result = chatMutation.startChat(StartChatInput.newBuilder().userPrompt(userPrompt).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getChat()).isEqualTo(graphChat);
  }
}
