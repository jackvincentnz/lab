package nz.geek.jack.mops.core.adapter.api.ai;

import nz.geek.jack.mops.core.api.ai.Budget;
import org.springframework.stereotype.Component;

@Component
public class AiBudgetMapper {

  public Budget map(nz.geek.jack.mops.core.domain.budget.Budget budget) {
    return new Budget(budget.getId().toString(), budget.getName());
  }
}
