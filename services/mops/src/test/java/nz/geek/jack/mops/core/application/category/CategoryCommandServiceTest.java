package nz.geek.jack.mops.core.application.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest extends TestBase {

  @Mock CategoryRepository categoryRepository;

  @InjectMocks CategoryCommandService categoryCommandService;

  @Captor ArgumentCaptor<Category> categoryCaptor;

  @Test
  void create_savesCategoryWithName() {
    var command = new CreateCategoryCommand(randomString());

    categoryCommandService.create(command);

    verify(categoryRepository).save(categoryCaptor.capture());
    var category = categoryCaptor.getValue();
    assertThat(category.getName()).isEqualTo(command.name());
  }

  @Test
  void create_returnsCategory() {
    var command = new CreateCategoryCommand(randomString());
    var savedCategory = mock(Category.class);
    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

    var result = categoryCommandService.create(command);

    assertThat(result).isEqualTo(savedCategory);
  }

  @Test
  void addCategoryValue_savesValueWithName() {
    var category = Category.create(randomString());
    var command = new AddCategoryValueCommand(category.getId(), randomString());
    when(categoryRepository.getById(category.getId())).thenReturn(category);

    categoryCommandService.addCategoryValue(command);

    verify(categoryRepository).save(categoryCaptor.capture());
    var categoryValue = categoryCaptor.getValue().getValues().iterator().next();
    assertThat(categoryValue.getName()).isEqualTo(command.name());
  }

  @Test
  void addCategoryValue_returnsValue() {
    var category = Category.create(randomString());
    var command = new AddCategoryValueCommand(category.getId(), randomString());
    when(categoryRepository.getById(command.categoryId())).thenReturn(category);
    when(categoryRepository.save(any(Category.class))).thenReturn(category);

    var categoryValue = categoryCommandService.addCategoryValue(command);

    assertThat(categoryValue.getName()).isEqualTo(command.name());
  }
}
