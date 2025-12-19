package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nz.geek.jack.libs.ddd.domain.NotFoundException;
import nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ChatTest extends TestBase {

  @Test
  void start_setsId() {
    var chat = Chat.start(randomString());

    assertThat(chat.getId()).isNotNull();
  }

  @Test
  void start_registersEventWithId() {
    var chat = Chat.start(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatStartedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void start_addsContent() {
    var content = randomString();

    var chat = Chat.start(content);

    assertThat(chat.getMessages().get(0).getContent().orElseThrow()).isEqualTo(content);
  }

  @Test
  void start_registersEventWithContent() {
    var content = randomString();

    var chat = Chat.start(content);

    var event = AggregateTestUtils.getLastEvent(chat, ChatStartedEvent.class);
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void start_registersEventWithCreatedAt() {
    var chat = Chat.start(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatStartedEvent.class);
    assertThat(event.createdAt()).isEqualTo(chat.getCreatedAt());
  }

  @Test
  void start_addsPendingAssistantMessage() {
    var content = randomString();

    var chat = Chat.start(content);

    assertThat(chat.getMessages().get(1).getType()).isEqualTo(MessageType.ASSISTANT);
    assertThat(chat.getMessages().get(1).getStatus()).isEqualTo(MessageStatus.PENDING);
  }

  @Test
  void start_registersEventWithPendingAssistantMessageId() {
    var chat = Chat.start(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatStartedEvent.class);
    assertThat(event.pendingAssistantMessageId()).isEqualTo(chat.getMessages().get(1).getId());
  }

  @Test
  void addUserMessage_cancelsPendingAssistantMessage() {
    var chat = Chat.start(randomString());
    var assistantMessage = chat.getMessages().get(1);

    chat.addUserMessage(randomString());

    assertThat(assistantMessage.getStatus()).isEqualTo(MessageStatus.CANCELLED);
  }

  @Test
  void addUserMessage_registersMessageCancelledEventWithChatId() {
    var chat = Chat.start(randomString());

    chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageCancelledEvent.class);
    assertThat(event.id()).isEqualTo(chat.getId());
  }

  @Test
  void addUserMessage_registersMessageCancelledEventWithMessageId() {
    var chat = Chat.start(randomString());
    var assistantMessage = chat.getMessages().get(1);

    chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageCancelledEvent.class);
    assertThat(event.messageId()).isEqualTo(assistantMessage.getId());
  }

  @Test
  void addUserMessage_registersMessageCancelledEventWithTimestamp() {
    var chat = Chat.start(randomString());
    var assistantMessage = chat.getMessages().get(1);

    chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageCancelledEvent.class);
    assertThat(event.timestamp()).isEqualTo(assistantMessage.getTimestamp());
  }

  @Test
  void addUserMessage_addsMessage() {
    var chat = Chat.start(randomString());

    var message = chat.addUserMessage(randomString());

    assertThat(message).isSameAs(chat.getMessages().get(2));
  }

  @Test
  void addUserMessage_addsMessageWithType() {
    var chat = Chat.start(randomString());

    var message = chat.addUserMessage(randomString());

    assertThat(message.getType()).isEqualTo(MessageType.USER);
  }

  @Test
  void addUserMessage_addsMessageWithContent() {
    var chat = Chat.start(randomString());
    var content = randomString();

    var message = chat.addUserMessage(content);

    assertThat(message.getContent().orElseThrow()).isEqualTo(content);
  }

  @Test
  void addUserMessage_addsMessageWithTimestamp() {
    var chat = Chat.start(randomString());

    var message = chat.addUserMessage(randomString());

    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void addUserMessage_registersEventWithChatId() {
    var chat = Chat.start(randomString());

    chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void addUserMessage_registersEventWithContent() {
    var chat = Chat.start(randomString());
    var content = randomString();

    chat.addUserMessage(content);

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void addUserMessage_registersEventWithTimestamp() {
    var chat = Chat.start(randomString());

    var message = chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.timestamp()).isEqualTo(message.getTimestamp());
  }

  @Test
  void addUserMessage_addsPendingAssistantMessage() {
    var chat = Chat.start(randomString());

    chat.addUserMessage(randomString());

    assertThat(chat.getMessages().get(3).getType()).isEqualTo(MessageType.ASSISTANT);
    assertThat(chat.getMessages().get(3).getStatus()).isEqualTo(MessageStatus.PENDING);
  }

  @Test
  void addUserMessage_registersEventWithPendingAssistantMessageId() {
    var chat = Chat.start(randomString());

    chat.addUserMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.pendingAssistantMessageId()).isEqualTo(chat.getMessages().get(3).getId());
  }

  @Test
  void editUserMessage_preventsEditingAssistantMessages() {
    var chat = Chat.start(randomString());
    var assistantMessage = chat.getMessages().get(1);

    assertThatThrownBy(() -> chat.editUserMessage(assistantMessage.getId(), randomString()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void editUserMessage_editsMessage() {
    var originalContent = randomString();
    var editedContent = randomString();
    var chat = Chat.start(originalContent);

    var message = chat.getMessages().get(0);

    chat.editUserMessage(message.getId(), editedContent);

    assertThat(message.getContent()).hasValue(editedContent);
  }

  @Test
  void editUserMessage_clearsOtherMessages() {
    var chat = Chat.start(randomString());
    var messages = chat.getMessages();

    chat.addUserMessage(randomString());
    var userMessage = messages.get(0);
    var otherMessages = new ArrayList<>(messages.subList(1, messages.size()));

    chat.editUserMessage(userMessage.getId(), randomString());

    assertThat(chat.getMessages()).hasSize(2);
    assertThat(chat.getMessages()).contains(userMessage);
    assertThat(chat.getMessages()).doesNotContainAnyElementsOf(otherMessages);
  }

  @Test
  void editUserMessage_addsPendingAssistantMessage() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);
    var assistantMessage = chat.getMessages().get(1);

    chat.editUserMessage(userMessage.getId(), randomString());

    var newAssistantMessage = chat.getMessages().get(1);
    assertThat(newAssistantMessage.getType()).isEqualTo(MessageType.ASSISTANT);
    assertThat(newAssistantMessage.getStatus()).isEqualTo(MessageStatus.PENDING);
    assertThat(newAssistantMessage).isNotEqualTo(assistantMessage);
  }

  @Test
  void editUserMessage_registersEventWithChatId() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(0);

    chat.editUserMessage(message.getId(), randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageEditedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void editUserMessage_registersEventWithMessageId() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(0);

    chat.editUserMessage(message.getId(), randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageEditedEvent.class);
    assertThat(event.messageId()).isEqualTo(message.getId());
  }

  @Test
  void editUserMessage_registersEventWithContent() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(0);

    chat.editUserMessage(message.getId(), randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageEditedEvent.class);
    assertThat(event.content()).isEqualTo(message.getContent().orElseThrow());
  }

  @Test
  void editUserMessage_registersEventWithTimestamp() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(0);

    chat.editUserMessage(message.getId(), randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageEditedEvent.class);
    assertThat(event.timestamp()).isEqualTo(message.getTimestamp());
  }

  @Test
  void editUserMessage_registersEventWithPendingAssistantMessageId() {
    var chat = Chat.start(randomString());
    var userMessage = chat.getMessages().get(0);

    chat.editUserMessage(userMessage.getId(), randomString());

    var assistantMessage = chat.getMessages().get(1);

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageEditedEvent.class);
    assertThat(event.pendingAssistantMessageId()).isEqualTo(assistantMessage.getId());
  }

  @Test
  void addPendingToolCalls_throwsNotFoundForMissingMessage() {
    var chat = Chat.start(randomString());

    assertThatThrownBy(() -> chat.addPendingToolCalls(MessageId.create(), List.of()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void addPendingToolCalls_completesMessage() {
    var chat = Chat.start(randomString());
    var toolCalls = List.of(new ToolCall("id", "name", "args", ToolCallStatus.PENDING_APPROVAL));

    var assistantMessage = chat.getMessages().get(1);

    chat.addPendingToolCalls(assistantMessage.getId(), toolCalls);

    assertThat(assistantMessage.getStatus()).isEqualTo(MessageStatus.COMPLETED);
    assertThat(assistantMessage.getToolCalls()).containsExactlyElementsOf(toolCalls);
  }

  @Test
  void addPendingToolCalls_registersEventWithChatId() {
    var chat = Chat.start(randomString());
    var toolCalls = List.of(new ToolCall("id", "name", "args", ToolCallStatus.PENDING_APPROVAL));

    var assistantMessage = chat.getMessages().get(1);

    chat.addPendingToolCalls(assistantMessage.getId(), toolCalls);

    var event = AggregateTestUtils.getLastEvent(chat, PendingToolCallsAddedEvent.class);

    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void addPendingToolCalls_registersEventWithToolCalls() {
    var chat = Chat.start(randomString());
    var toolCalls = List.of(new ToolCall("id", "name", "args", ToolCallStatus.PENDING_APPROVAL));

    var assistantMessage = chat.getMessages().get(1);

    chat.addPendingToolCalls(assistantMessage.getId(), toolCalls);

    var event = AggregateTestUtils.getLastEvent(chat, PendingToolCallsAddedEvent.class);

    assertThat(event.toolCalls()).containsExactlyElementsOf(toolCalls);
  }

  @Test
  void completeMessage_throwsNotFoundForMissingMessage() {
    var chat = Chat.start(randomString());

    assertThatThrownBy(() -> chat.completeMessage(MessageId.create(), randomString()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void completeMessage_completesMessage() {
    var chat = Chat.start(randomString());
    var content = randomString();

    var assistantMessage = chat.getMessages().get(1);

    chat.completeMessage(assistantMessage.getId(), content);

    assertThat(assistantMessage.getStatus()).isEqualTo(MessageStatus.COMPLETED);
    assertThat(assistantMessage.getContent()).hasValue(content);
  }

  @Test
  void completeMessage_registersEventWithChatId() {
    var chat = Chat.start(randomString());
    var content = randomString();

    var assistantMessage = chat.getMessages().get(1);

    chat.completeMessage(assistantMessage.getId(), content);

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageCompletedEvent.class);

    assertThat(event.chatId()).isEqualTo(chat.getId());
    assertThat(event.messageId()).isEqualTo(assistantMessage.getId());
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void retryAssistantMessage_throwsNotFoundForMissingMessage() {
    var chat = Chat.start(randomString());

    assertThatThrownBy(() -> chat.retryAssistantMessage(MessageId.create()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void retryAssistantMessage_preventsRetryOfUserMessage() {
    var chat = Chat.start(randomString());

    assertThatThrownBy(() -> chat.retryAssistantMessage(chat.getMessages().get(0).getId()))
        .isInstanceOf(NotFoundException.class);
  }

  @Test
  void retryAssistantMessage_addsNewPendingAssistantMessage() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(1);

    chat.retryAssistantMessage(message.getId());

    var newMessage = chat.getMessages().get(1);
    assertThat(newMessage.getType()).isEqualTo(MessageType.ASSISTANT);
    assertThat(newMessage.getStatus()).isEqualTo(MessageStatus.PENDING);
    assertThat(newMessage.getId()).isNotEqualTo(message.getId());
  }

  @Test
  void retryAssistantMessage_clearsOtherMessages() {
    var chat = Chat.start(randomString());
    chat.addUserMessage(randomString());
    var messages = chat.getMessages();
    var userMessage = messages.get(0);
    var otherMessages = new ArrayList<>(messages.subList(1, messages.size()));

    chat.retryAssistantMessage(messages.get(1).getId());

    assertThat(chat.getMessages()).hasSize(2);
    assertThat(chat.getMessages()).contains(userMessage);
    assertThat(chat.getMessages()).doesNotContainAnyElementsOf(otherMessages);
  }

  @Test
  void retryAssistantMessage_registersEventWithChatId() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(1);

    chat.retryAssistantMessage(message.getId());

    var event = AggregateTestUtils.getLastEvent(chat, AssistantMessageRetriedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void retryAssistantMessage_registersEventWithRetriedMessageId() {
    var chat = Chat.start(randomString());
    var message = chat.getMessages().get(1);

    chat.retryAssistantMessage(message.getId());

    var event = AggregateTestUtils.getLastEvent(chat, AssistantMessageRetriedEvent.class);
    assertThat(event.retriedMessageId()).isEqualTo(message.getId());
  }

  @Test
  void retryAssistantMessage_registersEventWithPendingAssistantMessageId() {
    var chat = Chat.start(randomString());

    chat.retryAssistantMessage(chat.getMessages().get(1).getId());

    var event = AggregateTestUtils.getLastEvent(chat, AssistantMessageRetriedEvent.class);
    assertThat(event.pendingAssistantMessageId()).isEqualTo(chat.getMessages().get(1).getId());
  }

  @Test
  void retryAssistantMessage_registersEventWithTimestamp() {
    var chat = Chat.start(randomString());

    chat.retryAssistantMessage(chat.getMessages().get(1).getId());

    var event = AggregateTestUtils.getLastEvent(chat, AssistantMessageRetriedEvent.class);
    assertThat(event.timestamp()).isEqualTo(chat.getMessages().get(1).getTimestamp());
  }
}
