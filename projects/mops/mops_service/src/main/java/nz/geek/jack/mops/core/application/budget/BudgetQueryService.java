package nz.geek.jack.mops.core.application.budget;

import java.util.Collection;
import nz.geek.jack.mops.core.domain.budget.Budget;
import nz.geek.jack.mops.core.domain.budget.BudgetRepository;
import org.springframework.stereotype.Service;

@Service
public class BudgetQueryService {

  private final BudgetRepository budgetRepository;

  public BudgetQueryService(BudgetRepository budgetRepository) {
    this.budgetRepository = budgetRepository;
  }

  public Collection<Budget> findAll() {
    return budgetRepository.findAll();
  }
}
