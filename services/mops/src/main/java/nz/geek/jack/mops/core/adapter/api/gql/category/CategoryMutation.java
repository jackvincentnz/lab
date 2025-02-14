package nz.geek.jack.mops.core.adapter.api.gql.category;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueInput;
import nz.geek.jack.mops.api.gql.types.AddCategoryValueResponse;
import nz.geek.jack.mops.api.gql.types.CreateCategoryInput;
import nz.geek.jack.mops.api.gql.types.CreateCategoryResponse;
import nz.geek.jack.mops.core.application.category.AddCategoryValueCommand;
import nz.geek.jack.mops.core.application.category.CategoryCommandService;
import nz.geek.jack.mops.core.application.category.CreateCategoryCommand;
import nz.geek.jack.mops.core.domain.category.CategoryId;

@DgsComponent
public class CategoryMutation {

  private final CategoryCommandService categoryCommandService;

  private final CategoryMapper categoryMapper;

  public CategoryMutation(
      CategoryCommandService categoryCommandService, CategoryMapper categoryMapper) {
    this.categoryCommandService = categoryCommandService;
    this.categoryMapper = categoryMapper;
  }

  @DgsMutation
  public CreateCategoryResponse createCategory(@InputArgument("input") CreateCategoryInput input) {
    var command = new CreateCategoryCommand(input.getName());

    var category = categoryCommandService.create(command);

    return CreateCategoryResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .category(categoryMapper.map(category))
        .build();
  }

  @DgsMutation
  public AddCategoryValueResponse addCategoryValue(
      @InputArgument("input") AddCategoryValueInput input) {
    var command =
        new AddCategoryValueCommand(CategoryId.fromString(input.getCategoryId()), input.getName());

    var categoryValue = categoryCommandService.addCategoryValue(command);

    return AddCategoryValueResponse.newBuilder()
        .code(SC_CREATED)
        .success(true)
        .message(CREATED_MESSAGE)
        .categoryValue(categoryMapper.map(categoryValue))
        .build();
  }
}
