package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nz.geek.jack.mops.api.gql.types.LineItem;

@DgsComponent
public class LineItemDataFetcher {

  static final List<LineItem> ALL_LINE_ITEMS = new ArrayList<>();

  @DgsQuery
  public Collection<LineItem> allLineItems() {
    return ALL_LINE_ITEMS;
  }
}
