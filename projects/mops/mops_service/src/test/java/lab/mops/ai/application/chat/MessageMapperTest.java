package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.ai.domain.chat.Message;
import lab.mops.ai.domain.chat.MessageType;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest extends TestBase {

  MessageMapper messageMapper = new MessageMapper();

  @Test
  public void map_mapsUserMessage() {
    var content = randomString();
    var domainMessage = mock(Message.class);
    when(domainMessage.getType()).thenReturn(MessageType.USER);
    when(domainMessage.getContent()).thenReturn(Optional.of(content));

    var result = (UserMessage) messageMapper.map(domainMessage);

    assertThat(result.content()).isEqualTo(content);
  }

  @Test
  public void map_mapsAssistantMessage() {
    var content = randomString();
    var domainMessage = mock(Message.class);
    when(domainMessage.getType()).thenReturn(MessageType.ASSISTANT);
    when(domainMessage.getContent()).thenReturn(Optional.of(content));

    var result = (AssistantMessage) messageMapper.map(domainMessage);

    assertThat(result.getContent()).hasValue(content);
  }
}
