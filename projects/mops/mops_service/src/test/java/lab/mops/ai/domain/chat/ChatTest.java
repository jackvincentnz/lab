package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
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
  void addMessage_addsMessage() {
    var chat = Chat.start(randomString());

    var message = chat.addMessage(randomString());

    assertThat(message).isSameAs(chat.getMessages().get(2));
  }

  @Test
  void addMessage_addsMessageWithType() {
    var chat = Chat.start(randomString());

    var message = chat.addMessage(randomString());

    assertThat(message.getType()).isEqualTo(MessageType.USER);
  }

  @Test
  void addMessage_addsMessageWithContent() {
    var chat = Chat.start(randomString());
    var content = randomString();

    var message = chat.addMessage(content);

    assertThat(message.getContent().orElseThrow()).isEqualTo(content);
  }

  @Test
  void addMessage_addsMessageWithTimestamp() {
    var chat = Chat.start(randomString());

    var message = chat.addMessage(randomString());

    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void addMessage_registersEventWithChatId() {
    var chat = Chat.start(randomString());

    chat.addMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void addMessage_registersEventWithContent() {
    var chat = Chat.start(randomString());
    var content = randomString();

    chat.addMessage(content);

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void addMessage_registersEventWithTimestamp() {
    var chat = Chat.start(randomString());

    var message = chat.addMessage(randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.timestamp()).isEqualTo(message.getTimestamp());
  }

  @Test
  void addMessage_preventsNullContent() {
    var chat = Chat.start(randomString());

    assertThatThrownBy(() -> chat.addMessage(null)).isInstanceOf(NullPointerException.class);
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
}
