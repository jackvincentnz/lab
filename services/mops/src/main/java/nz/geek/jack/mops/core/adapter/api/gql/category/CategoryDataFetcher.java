package nz.geek.jack.mops.core.adapter.api.gql.category;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import java.util.stream.Collectors;
import nz.geek.jack.mops.api.gql.types.Category;
import nz.geek.jack.mops.core.application.category.CategoryQueryService;

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
}
