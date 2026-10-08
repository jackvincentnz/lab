package lab.mops.ai.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import lab.mops.ai.domain.chat.Chat;
import lab.mops.ai.domain.chat.ChatStartedEvent;
import lab.mops.ai.domain.chat.ToolCall;
import lab.mops.ai.domain.chat.ToolCallApprovedEvent;
import lab.mops.ai.domain.chat.ToolCallId;
import lab.mops.ai.domain.chat.ToolCallStatus;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ChatOutboxCallbackTest extends TestBase {
  @Test
  void onAfterSave_pendingAndApprovedEvents_enqueuesWork() {
    var outbox = mock(ChatOutbox.class);
    var chat = Chat.start(randomString());
    var started = (ChatStartedEvent) chat.domainEvents().iterator().next();
    var toolCall =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);
    chat.addPendingToolCalls(started.pendingAssistantMessageId(), List.of(toolCall));

    assertThat(new ChatOutboxCallback(outbox).onAfterSave(chat)).isSameAs(chat);

    verify(outbox).onPendingAssistantMessageAdded(started);
    verify(outbox)
        .onToolCallApproved(
            new ToolCallApprovedEvent(
                chat.getId(), started.pendingAssistantMessageId(), toolCall.id()));
  }
}
