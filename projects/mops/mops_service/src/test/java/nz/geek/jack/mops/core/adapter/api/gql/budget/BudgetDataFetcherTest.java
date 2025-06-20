package nz.geek.jack.mops.core.adapter.api.gql.budget;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.api.gql.types.Budget;
import nz.geek.jack.mops.core.application.budget.BudgetQueryService;
import nz.geek.jack.test.TestBase;
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

  private nz.geek.jack.mops.core.domain.budget.Budget newBudget() {
    return nz.geek.jack.mops.core.domain.budget.Budget.create(randomString());
  }
}
