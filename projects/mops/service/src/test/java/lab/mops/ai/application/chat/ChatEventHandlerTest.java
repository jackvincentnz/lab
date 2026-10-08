package lab.mops.ai.application.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.libs.ddd.domain.test.AggregateTestUtils;
import lab.mops.ai.application.chat.completions.AssistantMessage;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.application.chat.completions.Message;
import lab.mops.ai.application.chat.completions.ToolCall;
import lab.mops.ai.application.chat.completions.UserMessage;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
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

    chatEventHandler.onPendingAssistantMessageAdded(event);

    var failed = chat.getMessages().get(1);
    assertThat(failed.getStatus()).isEqualTo(lab.mops.ai.domain.chat.MessageStatus.FAILED);
    assertThat(failed.getContent().orElseThrow()).contains("completion limit");
    verify(completionService, times(ChatEventHandler.MAX_COMPLETIONS)).getResponse(anyList());
    verify(chatRepository).save(chat);
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
    var chat = Chat.start(randomString());
    var messageId = chat.getMessages().get(1).getId();
    var toolCallId = ToolCallId.create();
    var toolName = randomString();
    var toolCallArgs = randomString();
    chat.addPendingToolCalls(
        messageId,
        List.of(
            lab.mops.ai.domain.chat.ToolCall.of(
                toolCallId, toolName, toolCallArgs, ToolCallStatus.APPROVED)));
    var event = new ToolCallApprovedEvent(chat.getId(), messageId, toolCallId);
    when(chatRepository.getById(chat.getId())).thenReturn(chat);
    var tool = mockTool(toolName);
    when(toolProvider.getTools()).thenReturn(List.of(tool));
    var toolCallResult = randomString();
    when(tool.call(toolCallArgs)).thenReturn(toolCallResult);

    chatEventHandler.onToolCallApproved(event);
    chatEventHandler.onToolCallApproved(event);

    assertThat(chat.getToolCallById(messageId, toolCallId).result()).isEqualTo(toolCallResult);
    verify(tool, times(1)).call(toolCallArgs);
    verify(chatRepository, times(1)).save(chat);
  }

  @Test
  void onPendingAssistantMessageAdded_cancelledOrRemovedMessage_skipsCompletion() {
    var chat = Chat.start(randomString());
    var event = AggregateTestUtils.getLastEvent(chat, PendingAssistantMessageAddedEvent.class);
    chat.addUserMessage(randomString());
    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatEventHandler.onPendingAssistantMessageAdded(event);
    chat.editUserMessage(chat.getMessages().get(0).getId(), randomString());
    chatEventHandler.onPendingAssistantMessageAdded(event);

    verifyNoInteractions(completionService, toolProvider);
  }

  @Test
  void onToolCallApproved_rejectedTool_doesNotExecute() {
    var chat = Chat.start(randomString());
    var messageId = chat.getMessages().get(1).getId();
    var toolCallId = ToolCallId.create();
    chat.addPendingToolCalls(
        messageId,
        List.of(
            lab.mops.ai.domain.chat.ToolCall.of(
                toolCallId, randomString(), randomString(), ToolCallStatus.REJECTED)));
    when(chatRepository.getById(chat.getId())).thenReturn(chat);

    chatEventHandler.onToolCallApproved(
        new ToolCallApprovedEvent(chat.getId(), messageId, toolCallId));

    verifyNoInteractions(toolProvider);
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
