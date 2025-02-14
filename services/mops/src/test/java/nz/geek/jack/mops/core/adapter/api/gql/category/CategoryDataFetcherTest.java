package nz.geek.jack.mops.core.adapter.api.gql.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.application.category.CategoryQueryService;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryDataFetcherTest extends TestBase {

  @Mock CategoryQueryService categoryQueryService;

  @Mock CategoryMapper categoryMapper;

  @InjectMocks CategoryDataFetcher categoryDataFetcher;

  @Test
  void allCategories_mapsCategories() {
    var domainCategory = Category.create(randomString());
    var graphCategory = nz.geek.jack.mops.api.gql.types.Category.newBuilder().build();
    when(categoryQueryService.findAll()).thenReturn(List.of(domainCategory));
    when(categoryMapper.map(domainCategory)).thenReturn(graphCategory);

    var result = categoryDataFetcher.allCategories();

    assertThat(result).contains(graphCategory);
  }
}
