package lab.mops.iam.domain.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TenantIdTest {

  @Test
  void create_setsId() {
    var id = TenantId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = TenantId.create().toString();

    var id = TenantId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
