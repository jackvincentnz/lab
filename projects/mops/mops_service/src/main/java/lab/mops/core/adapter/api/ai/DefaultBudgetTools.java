package lab.mops.core.adapter.api.ai;

import java.util.Collection;
import lab.mops.core.api.ai.BudgetTools;
import lab.mops.core.application.budget.BudgetCommandService;
import lab.mops.core.application.budget.CreateBudgetCommand;
import lab.mops.core.application.budget.ResolvedBudgetQueryService;
import lab.mops.core.application.budget.data.ResolvedBudget;
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
  @Tool(
      description =
          """
    Returns all budgets, their line items, how they are categorized, and how much is planned to be spent when.

    You can use this tool to answer most questions about budgets and their contents.

    For example, to answer the question: How much is planned to be spent in the Annual Marketing Budget 2025 in Jan?

    1. Get all budgets.
    2. Identify the planned spending in January for the budget named: Annual Marketing Budget.
    3. Sum up the planned spend amounts.
    """)
  public Collection<ResolvedBudget> getAllBudgets() {
    // FIXME: Avoid context window overflow
    return budgetQueryService.resolveBudgets();
  }
}
