package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ChatTest extends TestBase {

  @Test
  void create_setsId() {
    var chat = Chat.create();

    assertThat(chat.getId()).isNotNull();
  }

  @Test
  void create_registersEventWithId() {
    var chat = Chat.create();

    var event = (ChatCreatedEvent) chat.domainEvents().iterator().next();
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void addMessage_addsMessage() {
    var chat = Chat.create();

    var message = chat.addMessage(MessageType.USER, randomString());

    assertThat(message).isSameAs(chat.getMessages().get(0));
  }

  @Test
  void addMessage_addsMessageWithType() {
    var chat = Chat.create();
    var type = MessageType.USER;

    var message = chat.addMessage(type, randomString());

    assertThat(message.getType()).isEqualTo(type);
  }

  @Test
  void addMessage_addsMessageWithContent() {
    var chat = Chat.create();
    var content = randomString();

    var message = chat.addMessage(MessageType.USER, content);

    assertThat(message.getContent()).isEqualTo(content);
  }

  @Test
  void addMessage_addsMessageWithTimestamp() {
    var chat = Chat.create();

    var message = chat.addMessage(MessageType.USER, randomString());

    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void addMessage_registersEventWithchatId() {
    var chat = Chat.create();

    chat.addMessage(MessageType.USER, randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.chatId()).isEqualTo(chat.getId());
  }

  @Test
  void addMessage_registersEventWithContent() {
    var chat = Chat.create();
    var content = randomString();

    chat.addMessage(MessageType.USER, content);

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void addMessage_registersEventWithTimestamp() {
    var chat = Chat.create();

    var message = chat.addMessage(MessageType.USER, randomString());

    var event = AggregateTestUtils.getLastEvent(chat, ChatMessageAddedEvent.class);
    assertThat(event.timestamp()).isEqualTo(message.getTimestamp());
  }

  @Test
  void addMessage_preventsNullType() {
    var chat = Chat.create();

    assertThatThrownBy(() -> chat.addMessage(null, randomString()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addMessage_preventsNullContent() {
    var chat = Chat.create();

    assertThatThrownBy(() -> chat.addMessage(MessageType.USER, null))
        .isInstanceOf(NullPointerException.class);
  }
}
