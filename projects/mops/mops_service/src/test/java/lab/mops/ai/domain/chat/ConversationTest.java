package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ConversationTest extends TestBase {

  @Test
  void create_setsId() {
    var conversation = Conversation.create();

    assertThat(conversation.getId()).isNotNull();
  }

  @Test
  void create_registersEventWithId() {
    var conversation = Conversation.create();

    var event = (ConversationCreatedEvent) conversation.domainEvents().iterator().next();
    assertThat(event.conversationId()).isEqualTo(conversation.getId());
  }

  @Test
  void addMessage_addsMessage() {
    var conversation = Conversation.create();

    var message = conversation.addMessage(MessageType.USER, randomString());

    assertThat(message).isSameAs(conversation.getMessages().get(0));
  }

  @Test
  void addMessage_addsMessageWithType() {
    var conversation = Conversation.create();
    var type = MessageType.USER;

    var message = conversation.addMessage(type, randomString());

    assertThat(message.getType()).isEqualTo(type);
  }

  @Test
  void addMessage_addsMessageWithContent() {
    var conversation = Conversation.create();
    var content = randomString();

    var message = conversation.addMessage(MessageType.USER, content);

    assertThat(message.getContent()).isEqualTo(content);
  }

  @Test
  void addMessage_addsMessageWithTimestamp() {
    var conversation = Conversation.create();

    var message = conversation.addMessage(MessageType.USER, randomString());

    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void addMessage_registersEventWithConversationId() {
    var conversation = Conversation.create();

    conversation.addMessage(MessageType.USER, randomString());

    var event = AggregateTestUtils.getLastEvent(conversation, ConversationMessageAddedEvent.class);
    assertThat(event.conversationId()).isEqualTo(conversation.getId());
  }

  @Test
  void addMessage_registersEventWithContent() {
    var conversation = Conversation.create();
    var content = randomString();

    conversation.addMessage(MessageType.USER, content);

    var event = AggregateTestUtils.getLastEvent(conversation, ConversationMessageAddedEvent.class);
    assertThat(event.content()).isEqualTo(content);
  }

  @Test
  void addMessage_registersEventWithTimestamp() {
    var conversation = Conversation.create();

    var message = conversation.addMessage(MessageType.USER, randomString());

    var event = AggregateTestUtils.getLastEvent(conversation, ConversationMessageAddedEvent.class);
    assertThat(event.timestamp()).isEqualTo(message.getTimestamp());
  }

  @Test
  void addMessage_preventsNullType() {
    var conversation = Conversation.create();

    assertThatThrownBy(() -> conversation.addMessage(null, randomString()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void addMessage_preventsNullContent() {
    var conversation = Conversation.create();

    assertThatThrownBy(() -> conversation.addMessage(MessageType.USER, null))
        .isInstanceOf(NullPointerException.class);
  }
}
