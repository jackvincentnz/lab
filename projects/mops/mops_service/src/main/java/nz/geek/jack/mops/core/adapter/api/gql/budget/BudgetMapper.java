package nz.geek.jack.mops.core.adapter.api.gql.budget;

import nz.geek.jack.mops.api.gql.types.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

  public Budget map(nz.geek.jack.mops.core.domain.budget.Budget budget) {
    return Budget.newBuilder().id(budget.getId().toString()).name(budget.getName()).build();
  }
}
