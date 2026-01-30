package lab.mops.core.application.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.BudgetRepository;
import lab.mops.core.domain.budget.Categorization;
import lab.mops.core.domain.budget.LineItem;
import lab.mops.core.domain.budget.LineItemRepository;
import lab.mops.core.domain.budget.Spend;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryRepository;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemCommandServiceTest extends TestBase {

  @Mock BudgetRepository budgetRepository;

  @Mock LineItemRepository lineItemRepository;

  @Mock CategoryRepository categoryRepository;

  @InjectMocks LineItemCommandService lineItemCommandService;

  @Captor ArgumentCaptor<LineItem> lineItemCaptor;

  @Test
  void add_addsLineItemToBudget() {
    var budget = newBudget();
    var command = new AddLineItemCommand(budget.getId(), randomString());

    when(budgetRepository.getById(budget.getId())).thenReturn(budget);

    lineItemCommandService.add(command);

    verify(lineItemRepository).save(lineItemCaptor.capture());
    assertThat(lineItemCaptor.getValue().getBudgetId()).isEqualTo(budget.getId());
  }

  @Test
  void add_addsLineItemWithName() {
    var budget = newBudget();
    var name = randomString();
    var command = new AddLineItemCommand(budget.getId(), name);

    when(budgetRepository.getById(budget.getId())).thenReturn(budget);

    lineItemCommandService.add(command);

    verify(lineItemRepository).save(lineItemCaptor.capture());
    assertThat(lineItemCaptor.getValue().getName()).isEqualTo(name);
  }

  @Test
  void add_addsLineItemWithCategorization() {
    var budget = newBudget();
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var categorization = Categorization.of(category.getId(), categoryValue.getId());

    when(budgetRepository.getById(budget.getId())).thenReturn(budget);
    when(categoryRepository.mapById(Set.of(category.getId())))
        .thenReturn(Map.of(category.getId(), category));

    var command =
        new AddLineItemCommand(budget.getId(), randomString())
            .withCategorizations(List.of(categorization));

    lineItemCommandService.add(command);

    verify(lineItemRepository).save(lineItemCaptor.capture());
    assertThat(lineItemCaptor.getValue().getCategorizations()).isEqualTo(Set.of(categorization));
  }

  @Test
  void add_returnsSavedLineItem() {
    var budget = newBudget();
    var name = randomString();
    var command = new AddLineItemCommand(budget.getId(), name);
    var saved = budget.addLineItem(name);

    when(budgetRepository.getById(budget.getId())).thenReturn(budget);
    when(lineItemRepository.save(any(LineItem.class))).thenReturn(saved);

    var result = lineItemCommandService.add(command);

    assertThat(result).isEqualTo(saved);
  }

  @Test
  void planSpend_plansSpend() {
    var lineItem = newLineItem();
    var spend = Spend.of(LocalDate.now(), BigDecimal.valueOf(123.456));
    var command = new PlanSpendCommand(lineItem.getId(), spend);

    when(lineItemRepository.getById(lineItem.getId())).thenReturn(lineItem);
    when(lineItemRepository.save(lineItem)).thenReturn(lineItem);

    var result = lineItemCommandService.planSpend(command);

    assertThat(result.getSpending().size()).isEqualTo(1);
    assertThat(result.getSpending().iterator().next()).isEqualTo(spend);
  }

  @Test
  void categorize_addsCategorization() {
    var lineItem = newLineItem();
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    when(lineItemRepository.getById(lineItem.getId())).thenReturn(lineItem);
    when(categoryRepository.getById(category.getId())).thenReturn(category);
    when(lineItemRepository.save(lineItem)).thenReturn(lineItem);

    var result =
        lineItemCommandService.categorize(
            new CategorizeLineItemCommand(
                lineItem.getId(), category.getId(), categoryValue.getId()));

    var categorization = lineItem.getCategorizations().iterator().next();

    assertThat(categorization.getCategoryId()).isEqualTo(category.getId());
    assertThat(categorization.getCategoryValueId()).isEqualTo(categoryValue.getId());
    assertThat(result).isEqualTo(lineItem);
  }

  private Budget newBudget() {
    return Budget.create(randomString());
  }

  private LineItem newLineItem() {
    var budget = newBudget();
    return budget.addLineItem(randomString());
  }
}
