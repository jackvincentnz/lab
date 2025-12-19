package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessageIdTest {

  @Test
  void start_setsId() {
    var id = MessageId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = MessageId.create();

    var fromString = MessageId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_trueForSameId() {
    var id = MessageId.create();
    var fromString = MessageId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_falseForDifferentIds() {
    var id1 = MessageId.create();
    var id2 = MessageId.create();

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void hashCode_sameForEqualIds() {
    var id = MessageId.create();
    var fromString = MessageId.fromString(id.toString());

    assertThat(id.hashCode()).isEqualTo(fromString.hashCode());
  }
}
