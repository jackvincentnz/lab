package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class ToolCallIdTest extends TestBase {

  @Test
  void of_setsId() {
    var idString = randomId();
    var id = ToolCallId.of(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }

  @Test
  void create_setsId() {
    var id = ToolCallId.create();

    assertThat(id.toString()).isNotBlank();
  }

  @Test
  void equals_trueForSameId() {
    var id1 = ToolCallId.create();
    var id2 = ToolCallId.of(id1.toString());

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void equals_falseForDifferentIds() {
    var id1 = ToolCallId.create();
    var id2 = ToolCallId.create();

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void hashCode_sameForEqualIds() {
    var id = ToolCallId.create();
    var another = ToolCallId.of(id.toString());

    assertThat(id.hashCode()).isEqualTo(another.hashCode());
  }
}
