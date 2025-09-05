package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ChatIdTest {

  @Test
  void start_setsId() {
    var id = ChatId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = ChatId.create();

    var fromString = ChatId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_trueForSameId() {
    var id = ChatId.create();
    var fromString = ChatId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_falseForDifferentIds() {
    var id1 = ChatId.create();
    var id2 = ChatId.create();

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void hashCode_sameForEqualIds() {
    var id = ChatId.create();
    var fromString = ChatId.fromString(id.toString());

    assertThat(id.hashCode()).isEqualTo(fromString.hashCode());
  }
}
