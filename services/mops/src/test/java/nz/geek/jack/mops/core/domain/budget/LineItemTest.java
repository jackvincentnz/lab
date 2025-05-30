package nz.geek.jack.mops.core.domain.budget;

import static nz.geek.jack.libs.ddd.domain.test.AggregateTestUtils.getLastEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import nz.geek.jack.libs.ddd.domain.ValidationException;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class LineItemTest extends TestBase {

  @Test
  void add_setsId() {
    var lineItem = newLineItem();

    assertThat(lineItem.getId()).isNotNull();
  }

  @Test
  void add_registersEventWithId() {
    var lineItem = newLineItem();

    var event = (LineItemAddedEvent) lineItem.domainEvents().iterator().next();
    assertThat(event.id()).isEqualTo(lineItem.getId());
  }

  @Test
  void add_setsBudgetId() {
    var budget = newBudget();

    var lineItem = budget.addLineItem(randomString());

    assertThat(lineItem.getBudgetId()).isNotNull();
  }

  @Test
  void add_registersEventWithBudgetId() {
    var budget = newBudget();

    var lineItem = budget.addLineItem(randomString());

    var event = (LineItemAddedEvent) lineItem.domainEvents().iterator().next();
    assertThat(event.budgetId()).isEqualTo(lineItem.getBudgetId());
  }

  @Test
  void add_setsName() {
    var budget = newBudget();
    var name = randomString();

    var lineItem = budget.addLineItem(name);

    assertThat(lineItem.getName()).isEqualTo(name);
  }

  @Test
  void add_registersEventWithName() {
    var budget = newBudget();
    var name = randomString();

    var lineItem = budget.addLineItem(name);

    var event = (LineItemAddedEvent) lineItem.domainEvents().iterator().next();
    assertThat(event.name()).isEqualTo(lineItem.getName());
  }

  @Test
  void planSpend_preventsNull() {
    var lineItem = newLineItem();

    assertThatThrownBy(() -> lineItem.planSpend(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void planSpend_plansSpend() {
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(123.456));
    var lineItem = newLineItem();

    lineItem.planSpend(spend);

    assertThat(lineItem.getSpending().iterator().next()).isEqualTo(spend);
  }

  @Test
  void planSpend_registersEventWithId() {
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(123.456));
    var lineItem = newLineItem();

    lineItem.planSpend(spend);

    var event = getLastEvent(lineItem, SpendPlannedEvent.class);
    assertThat(event.id()).isEqualTo(lineItem.getId());
  }

  @Test
  void planSpend_registersEventWithSpend() {
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(123.456));
    var lineItem = newLineItem();

    lineItem.planSpend(spend);

    var event = getLastEvent(lineItem, SpendPlannedEvent.class);
    assertThat(event.spend()).isEqualTo(spend);
  }

  @Test
  void planSpend_preventsOverlappingSpend() {
    var spend1 = Spend.of(LocalDate.EPOCH, BigDecimal.valueOf(123.456));
    var spend2 = Spend.of(LocalDate.EPOCH, BigDecimal.valueOf(654.321));
    var lineItem = newLineItem();

    lineItem.planSpend(spend1);

    assertThatThrownBy(() -> lineItem.planSpend(spend2)).isInstanceOf(ValidationException.class);
  }

  private Budget newBudget() {
    return Budget.create(randomString());
  }

  private LineItem newLineItem() {
    var budget = newBudget();
    return budget.addLineItem(randomString());
  }
}
