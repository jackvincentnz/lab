package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.LineItem;
import nz.geek.jack.mops.core.application.lineitem.AddLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.LineItemCommandService;

@DgsComponent
public class LineItemMutation {

  protected static final String ADD_LINE_ITEM_SUCCESS_MESSAGE = "Line item was successfully added";

  private final LineItemCommandService lineItemCommandService;

  public LineItemMutation(LineItemCommandService lineItemCommandService) {
    this.lineItemCommandService = lineItemCommandService;
  }

  @DgsMutation
  public AddLineItemResponse addLineItem(@InputArgument("input") AddLineItemInput input) {
    var command = new AddLineItemCommand(input.getName());

    var lineItem = lineItemCommandService.add(command);

    return AddLineItemResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(ADD_LINE_ITEM_SUCCESS_MESSAGE)
        .lineItem(map(lineItem))
        .build();
  }

  private LineItem map(nz.geek.jack.mops.core.domain.lineitem.LineItem lineItem) {
    return LineItem.newBuilder().id(lineItem.getId().toString()).name(lineItem.getName()).build();
  }
}
