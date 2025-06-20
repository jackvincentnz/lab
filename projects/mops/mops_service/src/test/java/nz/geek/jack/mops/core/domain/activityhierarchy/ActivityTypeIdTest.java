package nz.geek.jack.mops.core.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityTypeIdTest {

  @Test
  void create_setsId() {
    var id = ActivityTypeId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = ActivityTypeId.create().toString();

    var id = ActivityTypeId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
