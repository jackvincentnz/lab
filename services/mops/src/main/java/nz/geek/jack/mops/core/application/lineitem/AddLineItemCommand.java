package nz.geek.jack.mops.core.application.lineitem;

import java.util.Collection;
import java.util.Optional;
import nz.geek.jack.mops.core.domain.budget.BudgetId;
import nz.geek.jack.mops.core.domain.budget.Categorization;

public final class AddLineItemCommand {

  private final BudgetId budgetId;

  private final String name;

  private Collection<Categorization> categorizations;

  public AddLineItemCommand(BudgetId budgetId, String name) {
    this.budgetId = budgetId;
    this.name = name;
  }

  public AddLineItemCommand withCategorizations(Collection<Categorization> categorizations) {
    this.categorizations = categorizations;
    return this;
  }

  public BudgetId getBudgetId() {
    return budgetId;
  }

  public String getName() {
    return name;
  }

  public Optional<Collection<Categorization>> getCategorizations() {
    return Optional.ofNullable(categorizations);
  }
}
