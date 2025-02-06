package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.lineitem.LineItemMutation.ADD_LINE_ITEM_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import nz.geek.jack.mops.core.application.lineitem.AddLineItemCommand;
import nz.geek.jack.mops.core.application.lineitem.LineItemCommandService;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemMutationTest extends TestBase {

  @Mock LineItemCommandService commandService;

  @InjectMocks LineItemMutation mutation;

  @Test
  void addLineItem_addsLineItem() {
    var lineItem = LineItem.add(randomString());
    when(commandService.add(new AddLineItemCommand(lineItem.getName()))).thenReturn(lineItem);

    mutation.addLineItem(AddLineItemInput.newBuilder().name(lineItem.getName()).build());

    verify(commandService).add(new AddLineItemCommand(lineItem.getName()));
  }

  @Test
  void addLineItem_returnsResponse() {
    var lineItem = LineItem.add(randomString());
    when(commandService.add(new AddLineItemCommand(lineItem.getName()))).thenReturn(lineItem);

    var result =
        mutation.addLineItem(AddLineItemInput.newBuilder().name(lineItem.getName()).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(ADD_LINE_ITEM_SUCCESS_MESSAGE);
    assertThat(result.getLineItem().getId()).isNotEmpty();
    assertThat(result.getLineItem().getName()).isEqualTo(lineItem.getName());
  }
}
