package nz.geek.jack.mops.core.adapter.startup;

import nz.geek.jack.mops.core.application.budget.BudgetCommandService;
import nz.geek.jack.mops.core.application.budget.BudgetQueryService;
import nz.geek.jack.mops.core.application.budget.CreateBudgetCommand;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BudgetCreator {

  protected static final String DEFAULT_BUDGET_NAME = "Default";

  private final BudgetQueryService budgetQueryService;

  private final BudgetCommandService budgetCommandService;

  public BudgetCreator(
      BudgetQueryService budgetQueryService, BudgetCommandService budgetCommandService) {
    this.budgetQueryService = budgetQueryService;
    this.budgetCommandService = budgetCommandService;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void onApplicationStarted() {
    if (budgetQueryService.findAll().isEmpty()) {
      createDefaultBudget();
    }
  }

  private void createDefaultBudget() {
    var command = new CreateBudgetCommand(DEFAULT_BUDGET_NAME);
    budgetCommandService.create(command);
  }
}
