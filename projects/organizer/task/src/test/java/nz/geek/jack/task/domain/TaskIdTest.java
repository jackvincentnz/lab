package nz.geek.jack.task.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskIdTest {

  @Test
  void create_setsId() {
    var id = TaskId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var idString = TaskId.create().toString();

    var id = TaskId.fromString(idString);

    assertThat(id.toString()).isEqualTo(idString);
  }
}
