package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.lineitem.LineItemDataFetcher.ALL_LINE_ITEMS;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import java.util.UUID;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.LineItem;

@DgsComponent
public class LineItemMutation {

  protected static final String ADD_LINE_ITEM_SUCCESS_MESSAGE = "Line item was successfully added";

  @DgsMutation
  public AddLineItemResponse addLineItem(@InputArgument("input") AddLineItemInput input) {

    var lineItem =
        LineItem.newBuilder().id(UUID.randomUUID().toString()).name(input.getName()).build();

    ALL_LINE_ITEMS.add(lineItem);

    return AddLineItemResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(ADD_LINE_ITEM_SUCCESS_MESSAGE)
        .lineItem(lineItem)
        .build();
  }
}
