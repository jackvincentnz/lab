package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LineItemDataFetcherTest {

  LineItemDataFetcher dataFetcher = new LineItemDataFetcher();

  @Test
  void allLineItems() {
    var result = dataFetcher.allLineItems();

    assertThat(result).isNotNull();
  }
}
