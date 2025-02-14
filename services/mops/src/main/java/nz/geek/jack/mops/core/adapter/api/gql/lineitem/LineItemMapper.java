package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import nz.geek.jack.mops.api.gql.types.Categorization;
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
                            .categoryId(c.getCategoryId().toString())
                            .categoryValueId(c.getCategoryValueId().toString())
                            .build())
                .toList())
        .build();
  }
}
