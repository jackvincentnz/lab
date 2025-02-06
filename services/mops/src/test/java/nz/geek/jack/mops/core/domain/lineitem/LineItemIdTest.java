package nz.geek.jack.mops.core.domain.lineitem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LineItemIdTest {

  @Test
  void create_setsId() {
    var id = LineItemId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = LineItemId.create();

    var fromString = LineItemId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }
}
