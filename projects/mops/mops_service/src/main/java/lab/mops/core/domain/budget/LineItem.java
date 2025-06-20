package lab.mops.core.domain.budget;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryValue;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import nz.geek.jack.libs.ddd.domain.ValidationException;

public class LineItem extends Aggregate<LineItemId> {

  private final BudgetId budgetId;

  private final String name;

  private final Set<Spend> spending;

  private final Set<Categorization> categorizations;

  private LineItem(
      LineItemId id,
      BudgetId budgetId,
      String name,
      Set<Spend> spending,
      Set<Categorization> categorizations) {
    super(id);
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(budgetId, "budgetId must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(spending, "spending must not be null");
    Objects.requireNonNull(categorizations, "categorizations must not be null");
    this.budgetId = budgetId;
    this.name = name;
    this.spending = spending;
    this.categorizations = categorizations;
  }

  public void planSpend(Spend spend) {
    Objects.requireNonNull(spend, "spend must not be null");

    if (spending.stream().anyMatch(s -> s.getSpendDay().equals(spend.getSpendDay()))) {
      throw new ValidationException(
          String.format(
              "Attempted to plan spend on [%s] when spending already exists for that day",
              spend.getSpendDay()));
    }

    this.spending.add(spend);

    registerEvent(new SpendPlannedEvent(this.id, spend));
  }

  public void categorize(Category category, CategoryValue categoryValue) {
    var categoryId = category.getId();
    var categoryValueId = categoryValue.getId();

    categorizations.add(Categorization.of(categoryId, categoryValueId));

    registerEvent(new LineItemCategorizedEvent(this.id, categoryId, categoryValueId));
  }

  public BudgetId getBudgetId() {
    return budgetId;
  }

  public String getName() {
    return name;
  }

  public Set<Spend> getSpending() {
    return spending;
  }

  public Set<Categorization> getCategorizations() {
    return categorizations;
  }

  static LineItem add(Budget budget, String name) {
    var lineItem =
        new LineItem(LineItemId.create(), budget.getId(), name, new HashSet<>(), new HashSet<>());

    lineItem.registerEvent(new LineItemAddedEvent(lineItem.id, budget.getId(), lineItem.getName()));

    return lineItem;
  }
}
