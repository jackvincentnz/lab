package lab.mops.core.api.gql.category;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static lab.mops.core.api.gql.ResponseMessage.CREATED_MESSAGE;
import static lab.mops.core.api.gql.ResponseMessage.OK_MESSAGE;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lab.mops.api.gql.types.AddCategoryValueInput;
import lab.mops.api.gql.types.AddCategoryValueResponse;
import lab.mops.api.gql.types.CreateCategoryInput;
import lab.mops.api.gql.types.CreateCategoryResponse;
import lab.mops.api.gql.types.UpdateCategoryNameInput;
import lab.mops.api.gql.types.UpdateCategoryNameResponse;
import lab.mops.api.gql.types.UpdateCategoryValueNameInput;
import lab.mops.api.gql.types.UpdateCategoryValueNameResponse;
import lab.mops.core.application.category.AddCategoryValueCommand;
import lab.mops.core.application.category.CategoryCommandService;
import lab.mops.core.application.category.CreateCategoryCommand;
import lab.mops.core.application.category.UpdateCategoryNameCommand;
import lab.mops.core.application.category.UpdateCategoryValueNameCommand;
import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;

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
  public UpdateCategoryNameResponse updateCategoryName(
      @InputArgument("input") UpdateCategoryNameInput input) {
    var command =
        new UpdateCategoryNameCommand(
            CategoryId.fromString(input.getCategoryId()), input.getName());

    var category = categoryCommandService.updateCategoryName(command);

    return UpdateCategoryNameResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
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

  @DgsMutation
  public UpdateCategoryValueNameResponse updateCategoryValueName(
      @InputArgument("input") UpdateCategoryValueNameInput input) {
    var command =
        new UpdateCategoryValueNameCommand(
            CategoryId.fromString(input.getCategoryId()),
            CategoryValueId.fromString(input.getCategoryValueId()),
            input.getName());

    var categoryValue = categoryCommandService.updateCategoryValueName(command);

    return UpdateCategoryValueNameResponse.newBuilder()
        .code(SC_OK)
        .success(true)
        .message(OK_MESSAGE)
        .categoryValue(categoryMapper.map(categoryValue))
        .build();
  }
}
