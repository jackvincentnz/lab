package nz.geek.jack.mops.core.application.budget;

import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.mops.core.domain.budget.BudgetRepository;
import org.springframework.stereotype.Component;

@Component
public class BudgetCommandService {

  private final BudgetRepository budgetRepository;

  public BudgetCommandService(BudgetRepository budgetRepository) {
    this.budgetRepository = budgetRepository;
  }

  public Budget create(CreateBudgetCommand command) {
    var budget = Budget.create(command.name());

    return budgetRepository.save(budget);
  }
}
