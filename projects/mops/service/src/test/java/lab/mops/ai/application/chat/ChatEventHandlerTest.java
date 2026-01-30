package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.libs.ddd.domain.test.AggregateTestUtils;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatId;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.MessageId;
import lab.mops.ai.domain.chat.PendingAssistantMessageAddedEvent;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.test.TestBase;
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

  @Mock ToolProvider toolProvider;

  @Mock ChatContextBuilder chatContextBuilder;

  @InjectMocks ChatEventHandler chatEventHandler;

  @Captor ArgumentCaptor<Chat> chatCaptor;

  @Captor ArgumentCaptor<List<lab.mops.ai.domain.chat.ToolCall>> toolCallsCaptor;

  @Test
  void onPendingAssistantMessageAdded_shouldCompleteMessageWhenResponseHasNoToolCalls() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);
    var completionsUserMessage = new UserMessage(userMessage.getContent().orElseThrow());
    List<Message> chatHistory = List.of(completionsUserMessage);
    var event = AggregateTestUtils.getLastEvent(chat, PendingAssistantMessageAddedEvent.class);
    var completion = AssistantMessage.of(randomString());

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(toolProvider.getTools()).thenReturn(List.of());
    when(chatContextBuilder.buildHistory(chat)).thenReturn(chatHistory);
    when(completionService.getResponse(chatHistory)).thenReturn(completion);

    chatEventHandler.onPendingAssistantMessageAdded(event);

    var assistantMessage = chat.getMessages().get(1);
    assertThat(assistantMessage.isCompleted()).isTrue();
    assertThat(assistantMessage.getContent()).isEqualTo(completion.getContent());
  }

  @Test
  void onPendingAssistantMessageAdded_shouldExecuteApprovedToolCallsAndCompleteMessage() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);
    var completionsUserMessage = new UserMessage(userMessage.getContent().orElseThrow());
    List<Message> chatHistory = List.of(completionsUserMessage);
    var event = AggregateTestUtils.getLastEvent(chat, PendingAssistantMessageAddedEvent.class);

    var toolName = randomString();
    var mockTool = mockTool(toolName, false);
    var toolCompletion =
        AssistantMessage.of(List.of(new ToolCall(randomString(), toolName, randomString())));
    var finalCompletion = AssistantMessage.of(randomString());

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(toolProvider.getTools()).thenReturn(List.of(mockTool));
    when(chatContextBuilder.buildHistory(chat)).thenReturn(chatHistory);
    when(completionService.getResponse(anyList()))
        .thenReturn(toolCompletion)
        .thenReturn(finalCompletion);

    chatEventHandler.onPendingAssistantMessageAdded(event);

    var assistantMessage = chat.getMessages().get(1);
    assertThat(assistantMessage.isCompleted()).isTrue();
    assertThat(assistantMessage.getContent()).isEqualTo(finalCompletion.getContent());
  }

  @Test
  void onPendingAssistantMessageAdded_shouldMaxOutLoops() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);
    var completionsUserMessage = new UserMessage(userMessage.getContent().orElseThrow());
    List<Message> chatHistory = List.of(completionsUserMessage);
    var event = AggregateTestUtils.getLastEvent(chat, PendingAssistantMessageAddedEvent.class);

    var toolName = randomString();
    var mockTool = mockTool(toolName, false);
    var toolCompletion =
        AssistantMessage.of(List.of(new ToolCall(randomString(), toolName, randomString())));
    var finalCompletion =
        AssistantMessage.of(List.of(new ToolCall(randomString(), toolName, randomString())));

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(toolProvider.getTools()).thenReturn(List.of(mockTool));
    when(chatContextBuilder.buildHistory(chat)).thenReturn(chatHistory);
    when(completionService.getResponse(anyList()))
        .thenReturn(toolCompletion)
        .thenReturn(finalCompletion);

    assertThatThrownBy(() -> chatEventHandler.onPendingAssistantMessageAdded(event))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void onPendingAssistantMessageAdded_shouldAddPendingToolCallsWhenToolNeedsApproval() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);
    var completionsUserMessage = new UserMessage(userMessage.getContent().orElseThrow());
    List<Message> chatHistory = List.of(completionsUserMessage);
    var event = AggregateTestUtils.getLastEvent(chat, PendingAssistantMessageAddedEvent.class);

    var toolName = randomString();
    var mockTool = mockTool(toolName, true);
    var completion =
        AssistantMessage.of(List.of(new ToolCall(randomString(), toolName, randomString())));

    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    when(toolProvider.getTools()).thenReturn(List.of(mockTool));
    when(chatContextBuilder.buildHistory(chat)).thenReturn(chatHistory);
    when(completionService.getResponse(List.of(completionsUserMessage))).thenReturn(completion);

    chatEventHandler.onPendingAssistantMessageAdded(event);

    var assistantMessage = chat.getMessages().get(1);
    assertThat(assistantMessage.isCompleted()).isTrue();
    var toolCall = assistantMessage.getToolCalls().get(0);
    assertThat(toolCall.name()).isEqualTo(toolName);
    assertThat(toolCall.status()).isEqualTo(ToolCallStatus.PENDING_APPROVAL);
  }

  @Test
  void onToolCallApproved_shouldExecuteToolCallAndRecordResult() {
    var chatId = ChatId.create();
    var messageId = MessageId.create();
    var toolCallId = ToolCallId.create();
    var event = new ToolCallApprovedEvent(chatId, messageId, toolCallId);

    var chat = mock(Chat.class);
    when(chatRepository.getById(chatId)).thenReturn(chat);

    var toolCall = mock(lab.mops.ai.domain.chat.ToolCall.class);
    var toolName = randomString();
    var toolCallArgs = randomString();
    when(toolCall.id()).thenReturn(toolCallId);
    when(toolCall.name()).thenReturn(toolName);
    when(toolCall.arguments()).thenReturn(toolCallArgs);

    when(chat.getToolCallById(event.messageId(), event.toolCallId())).thenReturn(toolCall);

    var tool = mockTool(toolName);
    when(toolProvider.getTools()).thenReturn(List.of(tool));

    var toolCallResult = randomString();
    when(tool.call(toolCallArgs)).thenReturn(toolCallResult);

    chatEventHandler.onToolCallApproved(event);

    verify(chat).recordToolResult(messageId, toolCallId, toolCallResult);
    verify(chatRepository).save(chat);
  }

  Tool mockTool(String name, boolean needsApproval) {
    var mockTool = mock(Tool.class);
    var mockToolDef = mock(ToolDefinition.class);
    when(mockToolDef.name()).thenReturn(name);
    when(mockToolDef.needsApproval()).thenReturn(needsApproval);
    when(mockTool.getToolDefinition()).thenReturn(mockToolDef);
    return mockTool;
  }

  Tool mockTool(String name) {
    var mockTool = mock(Tool.class);
    var mockToolDef = mock(ToolDefinition.class);
    when(mockToolDef.name()).thenReturn(name);
    when(mockTool.getToolDefinition()).thenReturn(mockToolDef);
    return mockTool;
  }
}
