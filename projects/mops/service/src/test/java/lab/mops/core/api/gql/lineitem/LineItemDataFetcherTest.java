package lab.mops.core.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import lab.mops.core.application.lineitem.LineItemQueryService;
import lab.mops.core.domain.budget.LineItem;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemDataFetcherTest extends TestBase {

  @Mock LineItemQueryService lineItemQueryService;

  @Mock LineItemMapper lineItemMapper;

  @InjectMocks LineItemDataFetcher dataFetcher;

  @Test
  void allLineItems_mapsLineItems() {
    var domainLineItem = mock(LineItem.class);
    var graphqLineItem = mock(lab.mops.api.gql.types.LineItem.class);
    when(lineItemQueryService.findAll()).thenReturn(List.of(domainLineItem));
    when(lineItemMapper.map(domainLineItem)).thenReturn(graphqLineItem);

    var result = dataFetcher.allLineItems().get(0);

    assertThat(result).isEqualTo(graphqLineItem);
  }
}
