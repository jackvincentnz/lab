package lab.mops.core.domain.asset;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AssetIdTest {

  @Test
  void create_setsId() {
    var id = AssetId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = AssetId.create();

    var fromString = AssetId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }
}
