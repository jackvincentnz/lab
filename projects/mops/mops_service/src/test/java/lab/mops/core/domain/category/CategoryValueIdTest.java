package lab.mops.core.domain.category;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryValueIdTest {

  @Test
  void create_setsId() {
    var id = CategoryValueId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = CategoryValueId.create();

    var fromString = CategoryValueId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }
}
