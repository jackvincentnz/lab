package nz.geek.jack.mops.plan.domain.activity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityIdTest {

  @Test
  void create_setsId() {
    var id = ActivityId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = ActivityId.create().toString();

    var id = ActivityId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
