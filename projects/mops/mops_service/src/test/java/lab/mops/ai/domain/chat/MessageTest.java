package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class MessageTest extends TestBase {

  @Test
  void assistantMessage_setsId() {
    var message = Message.assistantMessage();

    assertThat(message.getId()).isNotNull();
  }

  @Test
  void assistantMessage_setsType() {
    var message = Message.assistantMessage();

    assertThat(message.getType()).isEqualTo(MessageType.ASSISTANT);
  }

  @Test
  void assistantMessage_doesntSetContent() {
    var message = Message.assistantMessage();

    assertThat(message.getContent()).isEmpty();
  }

  @Test
  void assistantMessage_setsTimestamp() {
    var message = Message.assistantMessage();

    assertThat(message.getTimestamp()).isNotNull();
    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void assistantMessage_hasPendingStatus() {
    var message = Message.assistantMessage();

    assertThat(message.isPending()).isTrue();
  }

  @Test
  void userMessage_setsId() {
    var message = Message.userMessage(randomString());

    assertThat(message.getId()).isNotNull();
  }

  @Test
  void userMessage_setsType() {
    var message = Message.userMessage(randomString());

    assertThat(message.getType()).isEqualTo(MessageType.USER);
  }

  @Test
  void userMessage_setsContent() {
    var content = randomString();

    var message = Message.userMessage(content);

    assertThat(message.getContent()).contains(content);
  }

  @Test
  void userMessage_setsStatus() {
    var message = Message.userMessage(randomString());

    assertThat(message.getStatus()).isEqualTo(MessageStatus.COMPLETED);
  }

  @Test
  void userMessage_setsTimestamp() {
    var message = Message.userMessage(randomString());

    assertThat(message.getTimestamp()).isNotNull();
    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void userMessage_preventsNullContent() {
    assertThatThrownBy(() -> Message.userMessage(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void complete_setsContent() {
    var message = Message.assistantMessage();
    var content = randomString();

    assertThat(message.getContent()).isEmpty();

    message.complete(content);

    assertThat(message.getContent()).hasValue(content);
  }

  @Test
  void complete_setsCompleted() {
    var message = Message.assistantMessage();

    message.complete(randomString());

    assertThat(message.isCompleted()).isTrue();
  }

  @Test
  void complete_preventsNullContent() {
    var message = Message.assistantMessage();

    assertThatThrownBy(() -> message.complete(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void cancel_setsCancelledStatus() {
    var message = Message.assistantMessage();

    message.cancel();

    assertThat(message.isCancelled()).isTrue();
  }

  @Test
  void edit_preventsNullContent() {
    var message = Message.userMessage(randomString());

    assertThatThrownBy(() -> message.edit(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void edit_setsContent() {
    var message = Message.userMessage(randomString());
    var content = randomString();

    message.edit(content);

    assertThat(message.getContent()).hasValue(content);
  }
}
