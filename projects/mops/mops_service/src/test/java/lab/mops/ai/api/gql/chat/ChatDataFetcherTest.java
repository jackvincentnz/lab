package lab.mops.ai.api.gql.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lab.mops.ai.application.chat.ChatQueryService;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatDataFetcherTest extends TestBase {

  @Mock ChatQueryService chatQueryService;

  @Mock ChatMapper chatMapper;

  @InjectMocks ChatDataFetcher chatDataFetcher;

  @Test
  void chat_mapsChat() {
    var chatId = randomId();
    var domainChat = mock(Chat.class);
    var graphChat = mock(lab.mops.api.gql.types.Chat.class);

    when(chatQueryService.getById(ChatId.fromString(chatId))).thenReturn(domainChat);
    when(chatMapper.map(domainChat)).thenReturn(graphChat);

    var chat = chatDataFetcher.chat(chatId);

    assertThat(chat).isEqualTo(graphChat);
  }
}
