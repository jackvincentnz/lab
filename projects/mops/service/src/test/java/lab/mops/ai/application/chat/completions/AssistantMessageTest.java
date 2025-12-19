package lab.mops.ai.application.chat.completions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class AssistantMessageTest extends TestBase {

  @Test
  void getRole_returnsAssistant() {
    var message = AssistantMessage.of(randomString());

    assertThat(message.getRole()).isEqualTo(MessageRole.ASSISTANT);
  }

  @Test
  void of_setsContentAndToolCalls() {
    var content = randomString();
    var toolCalls = List.of(new ToolCall(randomString(), randomString(), randomString()));
    var message = AssistantMessage.of(content, toolCalls);

    assertThat(message.getContent()).hasValue(content);
    assertThat(message.getToolCalls()).containsExactlyElementsOf(toolCalls);
  }

  @Test
  void of_setsContent() {
    var content = randomString();
    var message = AssistantMessage.of(content);

    assertThat(message.getContent()).hasValue(content);
    assertThat(message.getToolCalls()).isEmpty();
  }

  @Test
  void of_setsToolCalls() {
    var toolCalls = List.of(new ToolCall(randomString(), randomString(), randomString()));
    var message = AssistantMessage.of(toolCalls);

    assertThat(message.getToolCalls()).containsExactlyElementsOf(toolCalls);
    assertThat(message.getContent()).isEmpty();
  }
}
