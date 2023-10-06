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

  @Test
  void testEquals_equalsSameObject() {
    var id1 = TaskId.create();
    var id2 = id1;

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void testEquals_isNotEqualForNull() {
    var id1 = TaskId.create();

    assertThat(id1).isNotEqualTo(null);
  }

  @Test
  void testEquals_isNotEqualForObject() {
    var id1 = TaskId.create();

    assertThat(id1).isNotEqualTo(new Object());
  }

  @Test
  void testEquals_isNotEqualForOtherId() {
    var id1 = TaskId.create();
    var id2 = TaskId.create();

    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  void testEquals_isEqualForMatchingString() {
    var id1 = TaskId.create();
    var id2 = TaskId.fromString(id1.toString());

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void testHashCode_isSameForMatchingIds() {
    var id1 = TaskId.create();
    var id2 = TaskId.fromString(id1.toString());

    assertThat(id1).isNotSameAs(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }
}
