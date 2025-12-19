package lab.mops.core.domain.budget;

import java.util.Objects;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public class Budget extends Aggregate<BudgetId> {

  private final String name;

  private Budget(BudgetId id, String name) {
    super(id);
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    this.name = name;
  }

  public LineItem addLineItem(String name) {
    return LineItem.add(this, name);
  }

  public String getName() {
    return name;
  }

  public static Budget create(String name) {
    var budget = new Budget(BudgetId.create(), name);

    budget.registerEvent(new BudgetCreatedEvent(budget.id, budget.getName()));

    return budget;
  }
}
