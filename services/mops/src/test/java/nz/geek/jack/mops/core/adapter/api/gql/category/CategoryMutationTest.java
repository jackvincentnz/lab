package nz.geek.jack.mops.core.adapter.api.gql.category;

import static jakarta.servlet.http.HttpServletResponse.SC_CREATED;
import static nz.geek.jack.mops.core.adapter.api.gql.ResponseMessage.CREATED_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.mops.api.gql.types.AddCategoryValueInput;
import nz.geek.jack.mops.api.gql.types.CreateCategoryInput;
import nz.geek.jack.mops.core.application.category.AddCategoryValueCommand;
import nz.geek.jack.mops.core.application.category.CategoryCommandService;
import nz.geek.jack.mops.core.application.category.CreateCategoryCommand;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryId;
import nz.geek.jack.mops.core.domain.category.CategoryValue;
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
    var graphCategory = nz.geek.jack.mops.api.gql.types.Category.newBuilder().build();

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
    var graphCategoryValue = mock(nz.geek.jack.mops.api.gql.types.CategoryValue.class);

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
}
