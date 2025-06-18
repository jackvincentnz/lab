package nz.geek.jack.mops.core.adapter.api.ai;

import java.util.Collection;
import java.util.stream.Collectors;
import nz.geek.jack.mops.core.api.ai.Budget;
import nz.geek.jack.mops.core.api.ai.BudgetTools;
import nz.geek.jack.mops.core.application.budget.BudgetCommandService;
import nz.geek.jack.mops.core.application.budget.BudgetQueryService;
import nz.geek.jack.mops.core.application.budget.CreateBudgetCommand;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class DefaultBudgetTools implements BudgetTools {

  private final BudgetQueryService budgetQueryService;

  private final BudgetCommandService budgetCommandService;

  private final AiBudgetMapper budgetMapper;

  public DefaultBudgetTools(
      BudgetQueryService budgetQueryService,
      BudgetCommandService budgetCommandService,
      AiBudgetMapper budgetMapper) {
    this.budgetQueryService = budgetQueryService;
    this.budgetCommandService = budgetCommandService;
    this.budgetMapper = budgetMapper;
  }

  @Override
  @Tool(description = "Create a budget")
  public void createBudget(String name) {
    budgetCommandService.create(new CreateBudgetCommand(name));
  }

  @Override
  @Tool(description = "Get all budgets")
  public Collection<Budget> getAllBudgets() {
    return budgetQueryService.findAll().stream()
        .map(budgetMapper::map)
        .collect(Collectors.toList());
  }
}
