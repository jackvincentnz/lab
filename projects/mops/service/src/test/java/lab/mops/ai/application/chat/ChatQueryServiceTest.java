package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatQueryServiceTest extends TestBase {

  @Mock ChatRepository chatRepository;

  @InjectMocks ChatQueryService chatQueryService;

  @Test
  void getById_delegatesToRepository() {
    var id = ChatId.create();
    var chat = mock(Chat.class);

    when(chatRepository.getById(id)).thenReturn(chat);

    assertThat(chatQueryService.getById(id)).isEqualTo(chat);
  }
}
