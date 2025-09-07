package lab.mops.core.api.gql.category;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static lab.mops.common.api.gql.ResponseMessage.CREATED_MESSAGE;
import static lab.mops.common.api.gql.ResponseMessage.OK_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.api.gql.types.AddCategoryValueInput;
import lab.mops.api.gql.types.CreateCategoryInput;
import lab.mops.api.gql.types.UpdateCategoryNameInput;
import lab.mops.api.gql.types.UpdateCategoryValueNameInput;
import lab.mops.core.application.category.AddCategoryValueCommand;
import lab.mops.core.application.category.CategoryCommandService;
import lab.mops.core.application.category.CreateCategoryCommand;
import lab.mops.core.application.category.UpdateCategoryNameCommand;
import lab.mops.core.application.category.UpdateCategoryValueNameCommand;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValue;
import lab.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryMutationTest extends TestBase {

  @Mock CategoryCommandService categoryCommandService;

  @Mock CategoryMapper categoryMapper;

  @InjectMocks CategoryMutation categoryMutation;

  @Test
  void createCategory_creates() {
    var name = randomString();

    categoryMutation.createCategory(CreateCategoryInput.newBuilder().name(name).build());

    verify(categoryCommandService).create(new CreateCategoryCommand(name));
  }

  @Test
  void createCategory_mapsResponse() {
    var name = randomString();
    var domainCategory = Category.create(name);
    var graphCategory = lab.mops.api.gql.types.Category.newBuilder().build();

    when(categoryCommandService.create(new CreateCategoryCommand(name))).thenReturn(domainCategory);
    when(categoryMapper.map(domainCategory)).thenReturn(graphCategory);

    var result =
        categoryMutation.createCategory(CreateCategoryInput.newBuilder().name(name).build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getCategory()).isEqualTo(graphCategory);
  }

  @Test
  void updateCategoryName_updatesName() {
    var categoryId = CategoryId.create();
    var name = randomString();

    categoryMutation.updateCategoryName(
        UpdateCategoryNameInput.newBuilder().categoryId(categoryId.toString()).name(name).build());

    verify(categoryCommandService)
        .updateCategoryName(new UpdateCategoryNameCommand(categoryId, name));
  }

  @Test
  void updateCategoryName_mapsResponse() {
    var categoryId = CategoryId.create();
    var name = randomString();
    var domainCategory = mock(Category.class);
    var graphCategory = mock(lab.mops.api.gql.types.Category.class);

    when(categoryCommandService.updateCategoryName(new UpdateCategoryNameCommand(categoryId, name)))
        .thenReturn(domainCategory);
    when(categoryMapper.map(domainCategory)).thenReturn(graphCategory);

    var result =
        categoryMutation.updateCategoryName(
            UpdateCategoryNameInput.newBuilder()
                .categoryId(categoryId.toString())
                .name(name)
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getCategory()).isEqualTo(graphCategory);
  }

  @Test
  void addCategoryValue_adds() {
    var categoryId = CategoryId.create();
    var name = randomString();

    categoryMutation.addCategoryValue(
        AddCategoryValueInput.newBuilder().categoryId(categoryId.toString()).name(name).build());

    verify(categoryCommandService).addCategoryValue(new AddCategoryValueCommand(categoryId, name));
  }

  @Test
  void addCategoryValue_mapsResponse() {
    var categoryId = CategoryId.create();
    var name = randomString();

    var domainCategoryValue = mock(CategoryValue.class);
    var graphCategoryValue = mock(lab.mops.api.gql.types.CategoryValue.class);

    when(categoryCommandService.addCategoryValue(new AddCategoryValueCommand(categoryId, name)))
        .thenReturn(domainCategoryValue);
    when(categoryMapper.map(domainCategoryValue)).thenReturn(graphCategoryValue);

    var result =
        categoryMutation.addCategoryValue(
            AddCategoryValueInput.newBuilder()
                .categoryId(categoryId.toString())
                .name(name)
                .build());

    assertThat(result.getCode()).isEqualTo(SC_CREATED);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(CREATED_MESSAGE);
    assertThat(result.getCategoryValue()).isEqualTo(graphCategoryValue);
  }

  @Test
  void updateCategoryValueName_updatesName() {
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();
    var name = randomString();

    categoryMutation.updateCategoryValueName(
        UpdateCategoryValueNameInput.newBuilder()
            .categoryId(categoryId.toString())
            .categoryValueId(categoryValueId.toString())
            .name(name)
            .build());

    verify(categoryCommandService)
        .updateCategoryValueName(
            new UpdateCategoryValueNameCommand(categoryId, categoryValueId, name));
  }

  @Test
  void updateCategoryValueName_mapsResponse() {
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();
    var name = randomString();

    var domainValue = mock(CategoryValue.class);
    var graphValue = mock(lab.mops.api.gql.types.CategoryValue.class);

    when(categoryCommandService.updateCategoryValueName(
            new UpdateCategoryValueNameCommand(categoryId, categoryValueId, name)))
        .thenReturn(domainValue);
    when(categoryMapper.map(domainValue)).thenReturn(graphValue);

    var result =
        categoryMutation.updateCategoryValueName(
            UpdateCategoryValueNameInput.newBuilder()
                .categoryId(categoryId.toString())
                .categoryValueId(categoryValueId.toString())
                .name(name)
                .build());

    assertThat(result.getCode()).isEqualTo(SC_OK);
    assertThat(result.getSuccess()).isTrue();
    assertThat(result.getMessage()).isEqualTo(OK_MESSAGE);
    assertThat(result.getCategoryValue()).isEqualTo(graphValue);
  }
}
