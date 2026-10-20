package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ToolCallTest extends TestBase {

  @Test
  void approve_approves() {
    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    toolCall.approve();

    assertThat(toolCall.isApproved()).isTrue();
  }

  @Test
  void recordResult_records() {
    var result = randomString();
    var toolCall =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);

    toolCall.recordResult(result);

    assertThat(toolCall.result()).isEqualTo(result);
  }

  @Test
  void reject_rejects() {
    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    toolCall.reject();

    assertThat(toolCall.isRejected()).isTrue();
  }

  @Test
  void isCompleted_approvedWithResult_isTrue() {
    var toolCall =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);
    toolCall.recordResult(randomString());

    assertThat(toolCall.isCompleted()).isTrue();
  }

  @Test
  void isCompleted_approvedWithoutResult_isFalse() {
    var toolCall =
        ToolCall.of(ToolCallId.create(), randomString(), randomString(), ToolCallStatus.APPROVED);

    assertThat(toolCall.isCompleted()).isFalse();
  }

  @Test
  void isRejected_afterReject_isTrue() {
    var toolCall =
        ToolCall.of(
            ToolCallId.create(), randomString(), randomString(), ToolCallStatus.PENDING_APPROVAL);

    toolCall.reject();

    assertThat(toolCall.isRejected()).isTrue();
    assertThat(toolCall.isApproved()).isFalse();
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
