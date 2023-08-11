package nz.geek.jack.autojournal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskCompletedEntryTest {

  @Test
  void from_buildsEntry() {
    var result = TaskCompletedEntry.from("Build software");

    assertThat(result.getMessage()).isEqualTo("Completed: Build software");
  }
}
