package lab.mops.ai.application.chat;

import static lab.mops.ai.application.chat.ChatContextBuilder.REJECTED_TOOL_CALL_RESULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolResultMessage;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.MessageType;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatContextBuilderTest extends TestBase {

  @Mock MessageMapper messageMapper;

  @InjectMocks ChatContextBuilder chatContextBuilder;

  @Test
  void buildHistory_shouldMapCompletedMessages() {
    var chat = mock(Chat.class);
    var completedMessage = mock(lab.mops.ai.domain.chat.Message.class);
    when(completedMessage.isCompleted()).thenReturn(true);
    var notCompletedMessage = mock(lab.mops.ai.domain.chat.Message.class);
    when(notCompletedMessage.isCompleted()).thenReturn(false);

    when(chat.getMessages()).thenReturn(List.of(completedMessage, notCompletedMessage));

    var mapped = mock(Message.class);
    when(messageMapper.map(completedMessage)).thenReturn(mapped);

    var history = chatContextBuilder.buildHistory(chat);

    assertThat(history).hasSize(1);
    assertThat(history.get(0)).isEqualTo(mapped);
  }

  @Test
  void buildHistory_shouldAddToolResults() {
    var chat = mock(Chat.class);
    var assistantMessage = mock(lab.mops.ai.domain.chat.Message.class);

    when(assistantMessage.isCompleted()).thenReturn(true);
    when(assistantMessage.getType()).thenReturn(MessageType.ASSISTANT);

    var toolCall = mock(ToolCall.class);
    when(toolCall.id()).thenReturn(ToolCallId.create());
    when(toolCall.name()).thenReturn(randomString());
    when(toolCall.result()).thenReturn(randomString());
    when(assistantMessage.getToolCalls()).thenReturn(List.of(toolCall));

    when(chat.getMessages()).thenReturn(List.of(assistantMessage));

    var mapped = mock(Message.class);
    when(messageMapper.map(assistantMessage)).thenReturn(mapped);

    var history = chatContextBuilder.buildHistory(chat);

    assertThat(history).hasSize(2);
    var toolResult = (ToolResultMessage) history.get(1);
    assertThat(toolResult.toolCallId()).isEqualTo(toolCall.id().toString());
    assertThat(toolResult.toolName()).isEqualTo(toolCall.name());
    assertThat(toolResult.content()).isEqualTo(toolCall.result());
  }

  @Test
  void buildHistory_shouldAddRejectedToolCalls() {
    var chat = mock(Chat.class);
    var assistantMessage = mock(lab.mops.ai.domain.chat.Message.class);

    when(assistantMessage.isCompleted()).thenReturn(true);
    when(assistantMessage.getType()).thenReturn(MessageType.ASSISTANT);

    var toolCall = mock(ToolCall.class);
    when(toolCall.id()).thenReturn(ToolCallId.create());
    when(toolCall.name()).thenReturn(randomString());
    when(toolCall.status()).thenReturn(ToolCallStatus.REJECTED);
    when(assistantMessage.getToolCalls()).thenReturn(List.of(toolCall));

    when(chat.getMessages()).thenReturn(List.of(assistantMessage));

    var mapped = mock(Message.class);
    when(messageMapper.map(assistantMessage)).thenReturn(mapped);

    var history = chatContextBuilder.buildHistory(chat);

    assertThat(history).hasSize(2);
    var toolResult = (ToolResultMessage) history.get(1);
    assertThat(toolResult.toolCallId()).isEqualTo(toolCall.id().toString());
    assertThat(toolResult.toolName()).isEqualTo(toolCall.name());
    assertThat(toolResult.content()).isEqualTo(REJECTED_TOOL_CALL_RESULT);
  }
}
