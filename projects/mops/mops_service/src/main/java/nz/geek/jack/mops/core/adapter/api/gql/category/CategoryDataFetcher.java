package nz.geek.jack.mops.core.adapter.api.gql.category;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import nz.geek.jack.mops.api.gql.DgsConstants;
import nz.geek.jack.mops.api.gql.types.Categorization;
import nz.geek.jack.mops.api.gql.types.Category;
import nz.geek.jack.mops.api.gql.types.CategoryValue;
import nz.geek.jack.mops.core.application.category.CategoryQueryService;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;
import org.dataloader.DataLoader;

@DgsComponent
public class CategoryDataFetcher {

  private final CategoryQueryService categoryQueryService;

  private final CategoryMapper categoryMapper;

  public CategoryDataFetcher(
      CategoryQueryService categoryQueryService, CategoryMapper categoryMapper) {
    this.categoryQueryService = categoryQueryService;
    this.categoryMapper = categoryMapper;
  }

  @DgsQuery
  public List<Category> allCategories() {
    return categoryQueryService.findAll().stream()
        .map(categoryMapper::map)
        .collect(Collectors.toList());
  }

  @DgsData(
      parentType = DgsConstants.CATEGORIZATION.TYPE_NAME,
      field = DgsConstants.CATEGORIZATION.Category)
  public CompletableFuture<Category> categoryFromCategorization(DgsDataFetchingEnvironment dfe) {
    Categorization categorization = dfe.getSource();
    var categoryId = categorization.getCategory().getId();

    return load(dfe, categoryId).thenApply(categoryMapper::map);
  }

  @DgsData(
      parentType = DgsConstants.CATEGORIZATION.TYPE_NAME,
      field = DgsConstants.CATEGORIZATION.CategoryValue)
  public CompletableFuture<CategoryValue> categoryValueFromCategorization(
      DgsDataFetchingEnvironment dfe) {
    Categorization categorization = dfe.getSource();
    var categoryId = categorization.getCategory().getId();
    var categoryValueId = categorization.getCategoryValue().getId();

    return load(dfe, categoryId)
        .thenApply(
            c -> categoryMapper.map(c.getValue(CategoryValueId.fromString(categoryValueId))));
  }

  private CompletableFuture<nz.geek.jack.mops.core.domain.category.Category> load(
      DgsDataFetchingEnvironment dfe, String categoryId) {
    DataLoader<CategoryId, nz.geek.jack.mops.core.domain.category.Category> dataLoader =
        dfe.getDataLoader(CategoryDataLoader.class);

    return dataLoader.load(CategoryId.fromString(categoryId));
  }
}
