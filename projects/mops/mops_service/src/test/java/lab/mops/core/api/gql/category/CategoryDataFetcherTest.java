package lab.mops.core.api.gql.category;

import static lab.mops.core.api.gql.category.CategoryDataLoader.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironment;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lab.mops.api.gql.types.Categorization;
import lab.mops.api.gql.types.Category;
import lab.mops.api.gql.types.CategoryValue;
import lab.mops.core.application.category.CategoryQueryService;
import nz.geek.jack.test.TestBase;
import org.dataloader.DataLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
class CategoryDataFetcherTest extends TestBase {

  @Mock CategoryQueryService categoryQueryService;

  @Mock CategoryMapper categoryMapper;

  @Mock DataLoader<Object, Object> dataLoader;

  @Mock DataFetchingEnvironment internalDfe;

  DgsDataFetchingEnvironment dfe;

  @InjectMocks CategoryDataFetcher categoryDataFetcher;

  @Test
  void allCategories_mapsCategories() {
    var domainCategory = newCategory();
    var graphCategory = Category.newBuilder().build();
    when(categoryQueryService.findAll()).thenReturn(List.of(domainCategory));
    when(categoryMapper.map(domainCategory)).thenReturn(graphCategory);

    var result = categoryDataFetcher.allCategories();

    assertThat(result).contains(graphCategory);
  }

  @Test
  void categoryFromCategorization_returnsCategory() {
    mockDfe();
    var domainCategory = newCategory();
    var graphCategory = mock(Category.class);

    var categorization =
        Categorization.newBuilder()
            .category(Category.newBuilder().id(domainCategory.getId().toString()).build())
            .build();

    when(internalDfe.getSource()).thenReturn(categorization);
    when(dataLoader.load(domainCategory.getId()))
        .thenReturn(CompletableFuture.completedFuture(domainCategory));
    when(categoryMapper.map(domainCategory)).thenReturn(graphCategory);

    var result = categoryDataFetcher.categoryFromCategorization(dfe).join();

    assertThat(result).isEqualTo(graphCategory);
  }

  @Test
  void categoryValueFromCategorization_returnsCategory() {
    mockDfe();
    var domainCategory = newCategory();
    var domainValue = domainCategory.addValue(randomString());
    var graphValue = mock(CategoryValue.class);

    var categorization =
        Categorization.newBuilder()
            .category(Category.newBuilder().id(domainCategory.getId().toString()).build())
            .categoryValue(CategoryValue.newBuilder().id(domainValue.getId().toString()).build())
            .build();

    when(internalDfe.getSource()).thenReturn(categorization);
    when(dataLoader.load(domainCategory.getId()))
        .thenReturn(CompletableFuture.completedFuture(domainCategory));
    when(categoryMapper.map(domainValue)).thenReturn(graphValue);

    var result = categoryDataFetcher.categoryValueFromCategorization(dfe).join();

    assertThat(result).isEqualTo(graphValue);
  }

  private lab.mops.core.domain.category.Category newCategory() {
    return lab.mops.core.domain.category.Category.create(randomString());
  }

  private void mockDfe() {
    dfe = new DgsDataFetchingEnvironment(internalDfe, mock(ApplicationContext.class));
    when(internalDfe.getDataLoader(NAME)).thenReturn(dataLoader);
  }
}
