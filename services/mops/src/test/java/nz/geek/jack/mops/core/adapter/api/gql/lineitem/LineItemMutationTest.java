package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.lineitem.LineItemDataFetcher.ALL_LINE_ITEMS;
import static nz.geek.jack.mops.core.adapter.api.gql.lineitem.LineItemMutation.ADD_LINE_ITEM_SUCCESS_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import nz.geek.jack.mops.api.gql.types.AddLineItemInput;
import org.junit.jupiter.api.Test;

class LineItemMutationTest {

  LineItemMutation mutation = new LineItemMutation();

  @Test
  void addLineItem_addsLineItem() {
    var name = randomString();

    var result = mutation.addLineItem(AddLineItemInput.newBuilder().name(name).build());

    assertThat(result).isNotNull();
    assertThat(ALL_LINE_ITEMS.size()).isEqualTo(1);
    assertThat(ALL_LINE_ITEMS.get(0).getName()).isEqualTo(name);
  }

  @Test
  void addLineItem_returnsResponse() {
    var name = randomString();

    var result = mutation.addLineItem(AddLineItemInput.newBuilder().name(name).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(ADD_LINE_ITEM_SUCCESS_MESSAGE);
    assertThat(result.getLineItem().getId()).isNotEmpty();
    assertThat(result.getLineItem().getName()).isEqualTo(name);
  }

  private String randomString() {
    return UUID.randomUUID().toString();
  }
}
