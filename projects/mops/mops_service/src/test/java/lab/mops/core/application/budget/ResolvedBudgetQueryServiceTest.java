package lab.mops.core.application.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.BudgetRepository;
import lab.mops.core.domain.budget.LineItemRepository;
import lab.mops.core.domain.budget.Spend;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResolvedBudgetQueryServiceTest extends TestBase {

  @Mock BudgetRepository budgetRepository;

  @Mock LineItemRepository lineItemRepository;

  @Mock CategoryRepository categoryRepository;

  @InjectMocks ResolvedBudgetQueryService resolvedBudgetQueryService;

  @Test
  void resolveBudgets_mapsBudgets() {
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var budget = Budget.create(randomString());
    var lineItem = budget.addLineItem(randomString());
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(randomInt()));

    lineItem.categorize(category, categoryValue);
    lineItem.planSpend(spend);

    when(budgetRepository.findAll()).thenReturn(List.of(budget));
    when(lineItemRepository.findByBudgetId(budget.getId())).thenReturn(List.of(lineItem));
    when(categoryRepository.mapById(Set.of(category.getId())))
        .thenReturn(Map.of(category.getId(), category));

    var result = resolvedBudgetQueryService.resolveBudgets();

    assertThat(result).hasSize(1);
    var resolvedBudget = result.iterator().next();
    assertThat(resolvedBudget.id()).isEqualTo(budget.getId().toString());
    assertThat(resolvedBudget.name()).isEqualTo(budget.getName());

    assertThat(resolvedBudget.lineItems()).hasSize(1);
    var resolvedLineItem = resolvedBudget.lineItems().iterator().next();
    assertThat(resolvedLineItem.id()).isEqualTo(lineItem.getId().toString());
    assertThat(resolvedLineItem.name()).isEqualTo(lineItem.getName());

    assertThat(resolvedLineItem.categorizations()).hasSize(1);
    var resolvedCategorization = resolvedLineItem.categorizations().iterator().next();
    assertThat(resolvedCategorization.categoryId()).isEqualTo(category.getId().toString());
    assertThat(resolvedCategorization.categoryName()).isEqualTo(category.getName());
    assertThat(resolvedCategorization.categoryValueId())
        .isEqualTo(categoryValue.getId().toString());
    assertThat(resolvedCategorization.categoryValueName()).isEqualTo(categoryValue.getName());

    assertThat(resolvedLineItem.spending()).hasSize(1);
    var resultSpend = lineItem.getSpending().iterator().next();
    assertThat(resultSpend.getSpendDay()).isEqualTo(spend.getSpendDay());
    assertThat(resultSpend.getAmount()).isEqualTo(spend.getAmount());
  }

  @Test
  void resolveBudgets_excludesCategorizationsThatArentFound() {
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var category2 = Category.create(randomString());
    var categoryValue2 = category.addValue(randomString());

    var budget = Budget.create(randomString());
    var lineItem = budget.addLineItem(randomString());

    lineItem.categorize(category, categoryValue);
    lineItem.categorize(category2, categoryValue2);

    when(budgetRepository.findAll()).thenReturn(List.of(budget));
    when(lineItemRepository.findByBudgetId(budget.getId())).thenReturn(List.of(lineItem));
    when(categoryRepository.mapById(Set.of(category.getId(), category2.getId())))
        .thenReturn(Map.of(category.getId(), category));

    var result = resolvedBudgetQueryService.resolveBudgets();

    var resolvedBudget = result.iterator().next();
    var resolvedLineItem = resolvedBudget.lineItems().iterator().next();
    assertThat(resolvedLineItem.categorizations()).hasSize(1);
  }
}
