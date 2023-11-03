package nz.geek.jack.journal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntryIdTest {

  @Test
  void create_setsId() {
    var id = EntryId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = EntryId.create().toString();

    var id = EntryId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
