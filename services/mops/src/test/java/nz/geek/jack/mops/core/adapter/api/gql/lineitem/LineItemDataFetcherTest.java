package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.application.lineitem.LineItemQueryService;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LineItemDataFetcherTest extends TestBase {

  @Mock LineItemQueryService lineItemQueryService;

  @InjectMocks LineItemDataFetcher dataFetcher;

  @Test
  void allLineItems_mapsLineItems() {
    var lineItem = LineItem.add(randomString());
    when(lineItemQueryService.findAll()).thenReturn(List.of(lineItem));

    var result = dataFetcher.allLineItems().get(0);

    assertThat(result.getId()).isEqualTo(lineItem.getId().toString());
    assertThat(result.getName()).isEqualTo(lineItem.getName());
  }
}
