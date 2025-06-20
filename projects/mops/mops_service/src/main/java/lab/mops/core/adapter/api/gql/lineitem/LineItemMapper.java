package lab.mops.core.adapter.api.gql.lineitem;

import lab.mops.api.gql.types.Categorization;
import lab.mops.api.gql.types.Category;
import lab.mops.api.gql.types.CategoryValue;
import lab.mops.api.gql.types.LineItem;
import lab.mops.api.gql.types.Spend;
import org.springframework.stereotype.Component;

@Component
public class LineItemMapper {

  public LineItem map(lab.mops.core.domain.budget.LineItem lineItem) {
    return LineItem.newBuilder()
        .id(lineItem.getId().toString())
        .budgetId(lineItem.getBudgetId().toString())
        .name(lineItem.getName())
        .categorizations(
            lineItem.getCategorizations().stream()
                .map(
                    c ->
                        Categorization.newBuilder()
                            .category(
                                Category.newBuilder().id(c.getCategoryId().toString()).build())
                            .categoryValue(
                                CategoryValue.newBuilder()
                                    .id(c.getCategoryValueId().toString())
                                    .build())
                            .build())
                .toList())
        .spending(
            lineItem.getSpending().stream()
                .map(s -> Spend.newBuilder().day(s.getSpendDay()).amount(s.getAmount()).build())
                .toList())
        .build();
  }
}
