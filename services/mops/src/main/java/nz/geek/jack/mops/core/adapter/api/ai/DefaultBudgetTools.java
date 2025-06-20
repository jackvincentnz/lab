package nz.geek.jack.mops.core.adapter.api.ai;

import java.util.Collection;
import nz.geek.jack.mops.core.api.ai.BudgetTools;
import nz.geek.jack.mops.core.application.budget.BudgetCommandService;
import nz.geek.jack.mops.core.application.budget.CreateBudgetCommand;
import nz.geek.jack.mops.core.application.budget.ResolvedBudgetQueryService;
import nz.geek.jack.mops.core.application.budget.data.ResolvedBudget;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DefaultBudgetTools implements BudgetTools {

  private final ResolvedBudgetQueryService budgetQueryService;

  private final BudgetCommandService budgetCommandService;

  public DefaultBudgetTools(
      ResolvedBudgetQueryService budgetQueryService, BudgetCommandService budgetCommandService) {
    this.budgetQueryService = budgetQueryService;
    this.budgetCommandService = budgetCommandService;
  }

  @Override
  @Tool(description = "Create a budget")
  public void createBudget(String name) {
    // FIXME: Ensure human-in-the-loop before calling tools that change state
    budgetCommandService.create(new CreateBudgetCommand(name));
  }

  @Override
  @Tool(description = "Get all budgets")
  public Collection<ResolvedBudget> getAllBudgets() {
    // FIXME: Avoid context window overflow
    return budgetQueryService.resolveBudgets();
  }
}
