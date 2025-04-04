package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.OK_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.AddLineItemResponse;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemInput;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemResponse;
import nz.geek.jack.mops.api.gql.types.DeleteAllLineItemsResponse;
import nz.geek.jack.mops.api.gql.types.PlanSpendInput;
import nz.geek.jack.mops.api.gql.types.PlanSpendResponse;
import nz.geek.jack.mops.core.application.lineitem.AddLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.CategorizeLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.LineItemCommandService;
import nz.geek.jack.mops.core.application.lineitem.PlanSpendCommand;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.mops.core.domain.lineitem.Categorization;
import nz.geek.jack.mops.core.domain.lineitem.LineItemId;
import nz.geek.jack.mops.core.domain.lineitem.Spend;

@DgsComponent
public class LineItemMutation {

  private final LineItemCommandService lineItemCommandService;

  private final LineItemMapper lineItemMapper;

  public LineItemMutation(
      LineItemCommandService lineItemCommandService, LineItemMapper lineItemMapper) {
    this.lineItemCommandService = lineItemCommandService;
    this.lineItemMapper = lineItemMapper;
  }

  @DgsMutation
  public AddLineItemResponse addLineItem(@InputArgument("input") AddLineItemInput input) {
    var command = new AddLineItemCommand(input.getName());

    if (input.getCategorizations() != null) {
      command.withCategorizations(
          input.getCategorizations().stream()
              .map(
                  c ->
                      Categorization.of(
                          CategoryId.fromString(c.getCategoryId()),
                          CategoryValueId.fromString(c.getCategoryValueId())))
              .toList());
    }

    var lineItem = lineItemCommandService.add(command);

    return AddLineItemResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .lineItem(lineItemMapper.map(lineItem))
        .build();
  }

  @DgsMutation
  public PlanSpendResponse planSpend(@InputArgument("input") PlanSpendInput input) {
    var command =
        new PlanSpendCommand(
            LineItemId.fromString(input.getLineItemId()),
            Spend.of(input.getSpendInput().getSpendDay(), input.getSpendInput().getAmount()));

    var lineItem = lineItemCommandService.planSpend(command);

    return PlanSpendResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
        .lineItem(lineItemMapper.map(lineItem))
        .build();
  }

  @DgsMutation
  public CategorizeLineItemResponse categorizeLineItem(
      @InputArgument("input") CategorizeLineItemInput input) {
    var command =
        new CategorizeLineItemCommand(
            LineItemId.fromString(input.getLineItemId()),
            CategoryId.fromString(input.getCategoryId()),
            CategoryValueId.fromString(input.getCategoryValueId()));

    var lineItem = lineItemCommandService.categorize(command);

    return CategorizeLineItemResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
        .lineItem(lineItemMapper.map(lineItem))
        .build();
  }

  @DgsMutation
  public DeleteAllLineItemsResponse deleteAllLineItems() {
    lineItemCommandService.deleteAll();

    return DeleteAllLineItemsResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
        .build();
  }
}
