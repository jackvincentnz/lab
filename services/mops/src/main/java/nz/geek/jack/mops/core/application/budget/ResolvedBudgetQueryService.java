package nz.geek.jack.mops.core.application.budget;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import nz.geek.jack.mops.core.application.budget.data.ResolvedBudget;
import nz.geek.jack.mops.core.application.budget.data.ResolvedCategorization;
import nz.geek.jack.mops.core.application.budget.data.ResolvedLineItem;
import nz.geek.jack.mops.core.application.budget.data.Spend;
import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.mops.core.domain.budget.BudgetRepository;
import nz.geek.jack.mops.core.domain.budget.Categorization;
import nz.geek.jack.mops.core.domain.budget.LineItem;
import nz.geek.jack.mops.core.domain.budget.LineItemRepository;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class ResolvedBudgetQueryService {

  private final BudgetRepository budgetRepository;

  private final LineItemRepository lineItemRepository;

  private final CategoryRepository categoryRepository;

  public ResolvedBudgetQueryService(
      BudgetRepository budgetRepository,
      LineItemRepository lineItemRepository,
      CategoryRepository categoryRepository) {
    this.budgetRepository = budgetRepository;
    this.lineItemRepository = lineItemRepository;
    this.categoryRepository = categoryRepository;
  }

  public Collection<ResolvedBudget> resolveBudgets() {
    return budgetRepository.findAll().stream().map(this::resolveBudget).toList();
  }

  private ResolvedBudget resolveBudget(Budget budget) {
    var lineItems = lineItemRepository.findByBudgetId(budget.getId());

    var lineItemCategories =
        lineItems.stream()
            .flatMap(l -> l.getCategorizations().stream())
            .map(Categorization::getCategoryId)
            .collect(Collectors.toSet());

    var categoriesById = categoryRepository.mapById(lineItemCategories);

    var resolvedLineItems =
        lineItems.stream().map(lineItem -> resolveLineItem(lineItem, categoriesById)).toList();

    return new ResolvedBudget(budget.getId().toString(), budget.getName(), resolvedLineItems);
  }

  private ResolvedLineItem resolveLineItem(
      LineItem lineItem, Map<CategoryId, Category> categoriesById) {
    var resolvedCategorizations =
        lineItem.getCategorizations().stream()
            .filter(c -> categoryAndValueExist(c, categoriesById))
            .map(c -> resolveCategorization(c, categoriesById))
            .toList();

    var resolvedSpending =
        lineItem.getSpending().stream()
            .map(spend -> new Spend(spend.getSpendDay(), spend.getAmount()))
            .toList();

    return new ResolvedLineItem(
        lineItem.getId().toString(), lineItem.getName(), resolvedCategorizations, resolvedSpending);
  }

  private boolean categoryAndValueExist(
      Categorization categorization, Map<CategoryId, Category> categoriesById) {
    return categoriesById.containsKey(categorization.getCategoryId())
        && categoriesById
            .get(categorization.getCategoryId())
            .findValue(categorization.getCategoryValueId())
            .isPresent();
  }

  private ResolvedCategorization resolveCategorization(
      Categorization categorization, Map<CategoryId, Category> categoriesById) {
    var category = categoriesById.get(categorization.getCategoryId());
    var categoryValue = category.getValue(categorization.getCategoryValueId());

    return new ResolvedCategorization(
        category.getId().toString(),
        category.getName(),
        categoryValue.getId().toString(),
        categoryValue.getName());
  }
}
