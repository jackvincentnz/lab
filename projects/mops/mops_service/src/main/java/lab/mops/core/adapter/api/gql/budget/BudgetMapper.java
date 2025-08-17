package lab.mops.core.adapter.api.gql.budget;

import lab.mops.api.gql.types.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

  public Budget map(lab.mops.core.domain.budget.Budget budget) {
    return Budget.newBuilder()
        .id(budget.getId().toString())
        .name(budget.getName())
        .createdAt(budget.getCreatedAt().toString())
        .updatedAt(budget.getUpdatedAt().toString())
        .build();
  }
}
