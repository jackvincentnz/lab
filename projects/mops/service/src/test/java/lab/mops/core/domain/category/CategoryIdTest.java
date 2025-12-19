package lab.mops.core.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryIdTest {

  @Test
  void create_setsId() {
    var id = CategoryId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = CategoryId.create();

    var fromString = CategoryId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }
}
