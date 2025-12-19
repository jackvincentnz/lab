package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BudgetIdTest {

  @Test
  void create_setsId() {
    var id = BudgetId.create();

    assertThat(id).isNotNull();
  }

  @Test
  void fromString_setsId() {
    var id = BudgetId.create();

    var fromString = BudgetId.fromString(id.toString());

    assertThat(id).isEqualTo(fromString);
  }
}
