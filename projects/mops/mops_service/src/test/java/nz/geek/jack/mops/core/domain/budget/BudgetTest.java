package nz.geek.jack.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class BudgetTest extends TestBase {

  @Test
  void create_preventsNullName() {
    assertThatThrownBy(() -> Budget.create(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void create_setsId() {
    var budget = Budget.create(randomString());

    assertThat(budget.getId()).isNotNull();
  }

  @Test
  void create_registersEventWithId() {
    var budget = Budget.create(randomString());

    var event = (BudgetCreatedEvent) budget.domainEvents().iterator().next();
    assertThat(event.id()).isEqualTo(budget.getId());
  }

  @Test
  void create_setsName() {
    var name = randomString();

    var budget = Budget.create(name);

    assertThat(budget.getName()).isEqualTo(name);
  }

  @Test
  void create_registersEventWithName() {
    var name = randomString();

    var budget = Budget.create(name);

    var event = (BudgetCreatedEvent) budget.domainEvents().iterator().next();
    assertThat(event.name()).isEqualTo(budget.getName());
  }
}
