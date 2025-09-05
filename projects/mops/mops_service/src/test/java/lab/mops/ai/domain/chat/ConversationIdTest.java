package lab.mops.ai.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConversationIdTest {

  @Test
  void create_setsId() {
    var id = ConversationId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = ConversationId.create();

    var fromString = ConversationId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_trueForSameId() {
    var id = ConversationId.create();
    var fromString = ConversationId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }

  @Test
  void equals_falseForDifferentIds() {
    var id1 = ConversationId.create();
    var id2 = ConversationId.create();

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void hashCode_sameForEqualIds() {
    var id = ConversationId.create();
    var fromString = ConversationId.fromString(id.toString());

    assertThat(id.hashCode()).isEqualTo(fromString.hashCode());
  }
}
