package lab.mops.core.application.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryRepository;
import lab.test.TestBase;
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
  void updateCategoryName_savesCategoryWithName() {
    var category = Category.create(randomString());
    var command = new UpdateCategoryNameCommand(category.getId(), randomString());
    when(categoryRepository.getById(category.getId())).thenReturn(category);

    categoryCommandService.updateCategoryName(command);

    verify(categoryRepository).save(categoryCaptor.capture());

    var result = categoryCaptor.getValue();
    assertThat(result.getName()).isEqualTo(command.name());
  }

  @Test
  void updateCategoryName_returnsCategory() {
    var category = Category.create(randomString());
    var command = new UpdateCategoryNameCommand(category.getId(), randomString());
    var savedCategory = mock(Category.class);

    when(categoryRepository.getById(category.getId())).thenReturn(category);
    when(categoryRepository.save(category)).thenReturn(savedCategory);

    var result = categoryCommandService.updateCategoryName(command);

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

  @Test
  void updateCategoryValueName_savesCategoryValueWithName() {
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var command =
        new UpdateCategoryValueNameCommand(category.getId(), categoryValue.getId(), randomString());

    when(categoryRepository.getById(category.getId())).thenReturn(category);

    categoryCommandService.updateCategoryValueName(command);

    verify(categoryRepository).save(categoryCaptor.capture());
    var result = categoryCaptor.getValue().getValues().iterator().next();
    assertThat(result.getName()).isEqualTo(command.name());
  }

  @Test
  void updateCategoryValueName_returnsValue() {
    var category = Category.create(randomString());
    var categoryValue = category.addValue(randomString());
    var command =
        new UpdateCategoryValueNameCommand(category.getId(), categoryValue.getId(), randomString());

    when(categoryRepository.getById(category.getId())).thenReturn(category);
    when(categoryRepository.save(category)).thenReturn(category);

    var result = categoryCommandService.updateCategoryValueName(command);

    assertThat(result.getName()).isEqualTo(command.name());
  }
}
