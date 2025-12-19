package lab.mops.core.api.ai;

import java.util.Collection;
import lab.mops.core.application.budget.data.ResolvedBudget;

public interface BudgetTools {

  void createBudget(String name);

  Collection<ResolvedBudget> getAllBudgets();
}
