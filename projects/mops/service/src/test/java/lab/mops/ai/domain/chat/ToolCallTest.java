package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ToolCallTest extends TestBase {

  @Test
  void approve_approves() {
    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    toolCall.approve();

    assertThat(toolCall.status()).isEqualTo(ToolCallStatus.APPROVED);
  }

  @Test
  void reject_rejects() {
    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    toolCall.reject();

    assertThat(toolCall.status()).isEqualTo(ToolCallStatus.REJECTED);
  }

  @Test
  void of_setsId() {
    var id = ToolCallId.create();

    var toolCall = ToolCall.of(id, randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    assertThat(toolCall.id()).isEqualTo(id);
  }

  @Test
  void of_setsName() {
    var name = randomString();

    var toolCall =
        ToolCall.of(ToolCallId.create(), name, randomString(), ToolCallStatus.PENDING_APPROVAL);

    assertThat(toolCall.name()).isEqualTo(name);
  }

  @Test
  void of_setsArguments() {
    var arguments = randomString();

    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), arguments, ToolCallStatus.PENDING_APPROVAL);

    assertThat(toolCall.arguments()).isEqualTo(arguments);
  }

  @Test
  void of_setsStatus() {
    var status = ToolCallStatus.PENDING_APPROVAL;

    var toolCall = ToolCall.of(ToolCallId.create(), randomString(), randomString(), status);

    assertThat(toolCall.status()).isEqualTo(status);
  }
}
