package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class MessageTest extends TestBase {

  @Test
  void create_setsType() {
    var message = Message.create(MessageType.USER, randomString());

    assertThat(message.getType()).isEqualTo(MessageType.USER);
  }

  @Test
  void create_setsContent() {
    var content = randomString();

    var message = Message.create(MessageType.USER, content);

    assertThat(message.getContent()).isEqualTo(content);
  }

  @Test
  void create_setsTimestamp() {
    var message = Message.create(MessageType.USER, randomString());

    assertThat(message.getTimestamp()).isNotNull();
    assertThat(message.getTimestamp()).isBefore(Instant.now().plusSeconds(1));
  }

  @Test
  void create_preventsNullType() {
    assertThatThrownBy(() -> Message.create(null, randomString()))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_preventsNullContent() {
    assertThatThrownBy(() -> Message.create(MessageType.USER, null))
        .isInstanceOf(NullPointerException.class);
  }
}
