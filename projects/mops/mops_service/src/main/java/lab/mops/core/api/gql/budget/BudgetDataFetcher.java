package lab.mops.core.api.gql.budget;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import lab.mops.api.gql.types.Budget;
import lab.mops.core.application.budget.BudgetQueryService;

@DgsComponent
public class BudgetDataFetcher {

  private final BudgetQueryService budgetQueryService;

  private final BudgetMapper budgetMapper;

  public BudgetDataFetcher(BudgetQueryService budgetQueryService, BudgetMapper budgetMapper) {
    this.budgetQueryService = budgetQueryService;
    this.budgetMapper = budgetMapper;
  }

  @DgsQuery
  public List<Budget> allBudgets() {
    return budgetQueryService.findAll().stream().map(budgetMapper::map).toList();
  }
}
