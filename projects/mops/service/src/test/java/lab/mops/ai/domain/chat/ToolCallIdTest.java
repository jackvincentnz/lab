package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class ToolCallIdTest extends TestBase {

  @Test
  void of_setsId() {
    var idString = randomId();
    var id = ToolCallId.of(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }

  @Test
  void equals_trueForSameId() {
    var id1 = ToolCallId.of(randomId());
    var id2 = ToolCallId.of(id1.toString());

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void equals_falseForDifferentIds() {
    var id1 = ToolCallId.of(randomId());
    var id2 = ToolCallId.of(randomId());

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void hashCode_sameForEqualIds() {
    var id = ToolCallId.of(randomId());
    var another = ToolCallId.of(id.toString());

    assertThat(id.hashCode()).isEqualTo(another.hashCode());
  }
}
