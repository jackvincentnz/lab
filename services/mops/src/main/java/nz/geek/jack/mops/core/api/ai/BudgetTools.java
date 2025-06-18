package nz.geek.jack.mops.core.api.ai;

import java.util.Collection;

public interface BudgetTools {

  void createBudget(String name);

  Collection<Budget> getAllBudgets();
}
