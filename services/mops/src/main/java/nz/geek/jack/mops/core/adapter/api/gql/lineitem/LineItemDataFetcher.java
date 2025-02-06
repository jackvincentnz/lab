package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import java.util.stream.Collectors;
import nz.geek.jack.mops.api.gql.types.LineItem;
import nz.geek.jack.mops.core.application.lineitem.LineItemQueryService;

@DgsComponent
public class LineItemDataFetcher {

  private final LineItemQueryService lineItemQueryService;

  public LineItemDataFetcher(LineItemQueryService lineItemQueryService) {
    this.lineItemQueryService = lineItemQueryService;
  }

  @DgsQuery
  public List<LineItem> allLineItems() {
    return lineItemQueryService.findAll().stream().map(this::map).collect(Collectors.toList());
  }

  private LineItem map(nz.geek.jack.mops.core.domain.lineitem.LineItem lineItem) {
    return LineItem.newBuilder().id(lineItem.getId().toString()).name(lineItem.getName()).build();
  }
}
