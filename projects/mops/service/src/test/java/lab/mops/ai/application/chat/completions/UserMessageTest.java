package lab.mops.ai.application.chat.completions;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class UserMessageTest extends TestBase {

  @Test
  void getRole() {
    var message = new UserMessage(randomString());

    assertThat(message.getRole()).isEqualTo(MessageRole.USER);
  }
}
