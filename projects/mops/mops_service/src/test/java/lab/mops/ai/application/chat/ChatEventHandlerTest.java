package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatMessageAddedEvent;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.Message;
import nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatEventHandlerTest extends TestBase {

  @Mock ChatRepository chatRepository;

  @Mock CompletionService completionService;

  @InjectMocks ChatEventHandler chatEventHandler;

  @Captor ArgumentCaptor<Chat> chatCaptor;

  @Test
  void onPendingAssistantMessageAdded_completeMessageWithCompletionResponse() {
    var chat = Chat.start(randomString());
    chat.addUserMessage(randomString());
    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    var response = randomString();

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(completionService.getResponse(
            chat.getMessages().stream().filter(Message::isCompleted).toList()))
        .thenReturn(response);

    chatEventHandler.onPendingAssistantMessageAdded(event);

    verify(chatRepository).save(chatCaptor.capture());

    var message = chatCaptor.getValue().getMessages().get(3);
    assertThat(message.getContent()).hasValue(response);
  }
}
