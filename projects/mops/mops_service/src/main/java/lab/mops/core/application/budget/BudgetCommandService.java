package lab.mops.core.application.budget;

import lab.mops.core.domain.budget.Budget;
import lab.mops.core.domain.budget.BudgetRepository;
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
