package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import nz.geek.jack.mops.api.gql.types.Categorization;
import nz.geek.jack.mops.api.gql.types.Category;
import nz.geek.jack.mops.api.gql.types.CategoryValue;
import nz.geek.jack.mops.api.gql.types.LineItem;
import org.springframework.stereotype.Component;

@Component
public class LineItemMapper {

  public LineItem map(nz.geek.jack.mops.core.domain.lineitem.LineItem lineItem) {
    return LineItem.newBuilder()
        .id(lineItem.getId().toString())
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
        .build();
  }
}
