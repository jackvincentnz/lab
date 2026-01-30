package lab.mops.ai.application.chat.completions;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ToolResultMessageTest extends TestBase {

  @Test
  void getRole() {
    var message = new ToolResultMessage(randomString(), randomString(), randomString());

    assertThat(message.getRole()).isEqualTo(MessageRole.TOOL);
  }
}
