package nz.geek.jack.mops.core.adapter.api.gql.category;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import java.util.List;
import java.util.stream.Collectors;
import nz.geek.jack.mops.api.gql.DgsConstants;
import nz.geek.jack.mops.api.gql.types.Categorization;
import nz.geek.jack.mops.api.gql.types.Category;
import nz.geek.jack.mops.api.gql.types.CategoryValue;
import nz.geek.jack.mops.core.application.category.CategoryQueryService;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValueId;

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
  public Category categoryFromCategorization(DgsDataFetchingEnvironment dfe) {
    Categorization categorization = dfe.getSource();
    var categoryId = CategoryId.fromString(categorization.getCategory().getId());

    // TODO: data loader
    var domainCategory = categoryQueryService.getById(categoryId);

    return categoryMapper.map(domainCategory);
  }

  @DgsData(
      parentType = DgsConstants.CATEGORIZATION.TYPE_NAME,
      field = DgsConstants.CATEGORIZATION.CategoryValue)
  public CategoryValue categoryValueFromCategorization(DgsDataFetchingEnvironment dfe) {
    Categorization categorization = dfe.getSource();
    var categoryId = CategoryId.fromString(categorization.getCategory().getId());
    var categoryValueId = CategoryValueId.fromString(categorization.getCategoryValue().getId());

    // TODO: data loader
    var domainCategory = categoryQueryService.getById(categoryId);

    return categoryMapper.map(domainCategory.getValue(categoryValueId));
  }
}
