package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.ai.domain.chat.Message;
import lab.mops.ai.domain.chat.MessageType;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
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
  public void map_mapsAssistantMessageWithContent() {
    var content = randomString();
    var domainMessage = mock(Message.class);
    when(domainMessage.getType()).thenReturn(MessageType.ASSISTANT);
    when(domainMessage.getContent()).thenReturn(Optional.of(content));

    var result = (AssistantMessage) messageMapper.map(domainMessage);

    assertThat(result.getContent()).hasValue(content);
  }

  @Test
  public void map_mapsAssistantMessageWithToolCalls() {
    var toolCallId = ToolCallId.create();
    var toolCallName = randomString();
    var toolCallArguments = randomString();
    var toolCall =
        ToolCall.of(toolCallId, toolCallName, toolCallArguments, ToolCallStatus.APPROVED);

    var domainMessage = mock(Message.class);
    when(domainMessage.getType()).thenReturn(MessageType.ASSISTANT);
    when(domainMessage.getContent()).thenReturn(Optional.empty());
    when(domainMessage.getToolCalls()).thenReturn(List.of(toolCall));

    var result = (AssistantMessage) messageMapper.map(domainMessage);

    var mappedToolCall = result.getToolCalls().iterator().next();
    assertThat(mappedToolCall.id()).isEqualTo(toolCallId.toString());
    assertThat(mappedToolCall.toolName()).isEqualTo(toolCallName);
    assertThat(mappedToolCall.arguments()).isEqualTo(toolCallArguments);
  }
}
