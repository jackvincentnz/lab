package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.OK_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.api.gql.types.CategorizeLineItemInput;
import nz.geek.jack.mops.core.application.lineitem.AddLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.CategorizeLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.LineItemCommandService;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.mops.core.domain.lineitem.LineItemId;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemMutationTest extends TestBase {

  @Mock LineItemCommandService lineItemCommandService;

  @Mock LineItemMapper lineItemMapper;

  @InjectMocks LineItemMutation lineItemMutation;

  @Test
  void addLineItem_addsLineItem() {
    var lineItem = LineItem.add(randomString());

    lineItemMutation.addLineItem(AddLineItemInput.newBuilder().name(lineItem.getName()).build());

    verify(lineItemCommandService).add(new AddLineItemCommand(lineItem.getName()));
  }

  @Test
  void addLineItem_mapsResponse() {
    var domainLineItem = LineItem.add(randomString());
    var graphLineItem = mock(nz.geek.jack.mops.api.gql.types.LineItem.class);

    when(lineItemCommandService.add(new AddLineItemCommand(domainLineItem.getName())))
        .thenReturn(domainLineItem);
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphLineItem);

    var result =
        lineItemMutation.addLineItem(
            AddLineItemInput.newBuilder().name(domainLineItem.getName()).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getLineItem()).isEqualTo(graphLineItem);
  }

  @Test
  public void categorizeLineItem_categorizes() {
    var lineItemId = LineItemId.create();
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();

    lineItemMutation.categorizeLineItem(
        CategorizeLineItemInput.newBuilder()
            .lineItemId(lineItemId.toString())
            .categoryId(categoryId.toString())
            .categoryValueId(categoryValueId.toString())
            .build());

    verify(lineItemCommandService)
        .categorize(new CategorizeLineItemCommand(lineItemId, categoryId, categoryValueId));
  }

  @Test
  public void categorizeLineItem_returnsResponse() {
    var lineItemId = LineItemId.create();
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();
    var domainLineItem = mock(LineItem.class);
    var graphLineItem = mock(nz.geek.jack.mops.api.gql.types.LineItem.class);

    when(lineItemCommandService.categorize(
            new CategorizeLineItemCommand(lineItemId, categoryId, categoryValueId)))
        .thenReturn(domainLineItem);
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphLineItem);

    var result =
        lineItemMutation.categorizeLineItem(
            CategorizeLineItemInput.newBuilder()
                .lineItemId(lineItemId.toString())
                .categoryId(categoryId.toString())
                .categoryValueId(categoryValueId.toString())
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getLineItem()).isEqualTo(graphLineItem);
  }
}
