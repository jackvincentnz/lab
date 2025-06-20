package nz.geek.jack.mops.core.api.ai;

import java.util.Collection;
import nz.geek.jack.mops.core.application.budget.data.ResolvedBudget;

public interface BudgetTools {

  void createBudget(String name);

  Collection<ResolvedBudget> getAllBudgets();
}
