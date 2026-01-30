package lab.mops.core.api.gql.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import lab.mops.core.application.category.CategoryQueryService;
import lab.mops.core.domain.category.Category;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryDataLoaderTest extends TestBase {

  @Mock CategoryQueryService categoryQueryService;

  @InjectMocks CategoryDataLoader categoryDataLoader;

  @Test
  void load() {
    var category1 = Category.create(randomString());
    var category2 = Category.create(randomString());

    var ids = Set.of(category1.getId(), category2.getId());
    var expectedMap = Map.of(category1.getId(), category1, category2.getId(), category2);

    when(categoryQueryService.mapById(ids)).thenReturn(expectedMap);

    var resultStage = categoryDataLoader.load(ids);

    resultStage
        .toCompletableFuture()
        .thenAccept(result -> assertThat(result).isEqualTo(expectedMap))
        .join();
  }
}
