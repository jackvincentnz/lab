package lab.mops.ai.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lab.mops.ai.application.chat.ChatContextBuilder;
import lab.mops.ai.application.chat.ChatEventHandler;
import lab.mops.ai.application.chat.Tool;
import lab.mops.ai.application.chat.ToolDefinition;
import lab.mops.ai.application.chat.completions.CompletionService;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatRepository;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

// Uses the real repository so that a concurrent save fails the chat's version check.
@SpringBootTest
@Import(FakeChatModelConfig.class)
@Transactional
class ChatEventHandlerConcurrencyTest extends TestBase {

  @Autowired ChatRepository chatRepository;

  @Test
  void onToolCallApproved_siblingResultSavedFirst_reloadsAndRecordsBothResults() {
    var chat = Chat.start(randomString());
    var messageId = chat.getMessages().get(1).getId();
    var call =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);
    var sibling =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);
    chat.addPendingToolCalls(messageId, List.of(call, sibling));
    chatRepository.save(chat);
    var result = randomString();
    var siblingResult = randomString();
    var tool = mockTool(call.name());
    when(tool.call(call.arguments())).thenReturn(result);
    var siblingSaved = new AtomicBoolean();
    var repository = mock(ChatRepository.class, delegatesTo(chatRepository));
    doAnswer(
            invocation -> {
              if (siblingSaved.compareAndSet(false, true)) {
                var latest = chatRepository.getById(chat.getId());
                latest.recordToolResult(messageId, sibling.id(), siblingResult);
                chatRepository.save(latest);
              }
              return chatRepository.save(invocation.<Chat>getArgument(0));
            })
        .when(repository)
        .save(any());
    var handler =
        new ChatEventHandler(
            repository,
            mock(CompletionService.class),
            () -> List.of(tool),
            mock(ChatContextBuilder.class));

    handler.onToolCallApproved(new ToolCallApprovedEvent(chat.getId(), messageId, call.id()));

    var saved = chatRepository.getById(chat.getId());
    assertThat(saved.getToolCallById(messageId, call.id()).result()).isEqualTo(result);
    assertThat(saved.getToolCallById(messageId, sibling.id()).result()).isEqualTo(siblingResult);
    assertThat(saved.getMessages()).hasSize(3);
    assertThat(saved.getMessages().get(2).isPending()).isTrue();
    verify(tool, times(1)).call(call.arguments());
  }

  private Tool mockTool(String name) {
    var tool = mock(Tool.class);
    var definition = mock(ToolDefinition.class);
    when(definition.name()).thenReturn(name);
    when(tool.getToolDefinition()).thenReturn(definition);
    return tool;
  }
}
