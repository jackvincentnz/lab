package lab.mops.core.api.gql.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.api.gql.types.Budget;
import lab.mops.core.application.budget.BudgetQueryService;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetDataFetcherTest extends TestBase {

  @Mock BudgetQueryService budgetQueryService;

  @Mock BudgetMapper budgetMapper;

  @InjectMocks BudgetDataFetcher budgetDataFetcher;

  @Test
  void allCategories_mapsCategories() {
    var domainBudget = newBudget();
    var graphBudget = Budget.newBuilder().build();
    when(budgetQueryService.findAll()).thenReturn(List.of(domainBudget));
    when(budgetMapper.map(domainBudget)).thenReturn(graphBudget);

    var result = budgetDataFetcher.allBudgets();

    assertThat(result).contains(graphBudget);
  }

  private lab.mops.core.domain.budget.Budget newBudget() {
    return lab.mops.core.domain.budget.Budget.create(randomString());
  }
}
