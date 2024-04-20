package nz.geek.jack.plan.domain.activityhierarchy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ActivityHierarchyIdTest {

  @Test
  void create_setsId() {
    var id = ActivityHierarchyId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = ActivityHierarchyId.create().toString();

    var id = ActivityHierarchyId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
