package lab.mops.core.api.gql.lineitem;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import lab.mops.api.gql.types.LineItem;
import lab.mops.core.application.lineitem.LineItemQueryService;

@DgsComponent
public class LineItemDataFetcher {

  private final LineItemQueryService lineItemQueryService;

  private final LineItemMapper lineItemMapper;

  public LineItemDataFetcher(
      LineItemQueryService lineItemQueryService, LineItemMapper lineItemMapper) {
    this.lineItemQueryService = lineItemQueryService;
    this.lineItemMapper = lineItemMapper;
  }

  @DgsQuery
  public List<LineItem> allLineItems() {
    return lineItemQueryService.findAll().stream().map(lineItemMapper::map).toList();
  }
}
