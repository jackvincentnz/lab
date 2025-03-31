package nz.geek.jack.mops.core.application.category;

import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.mops.core.domain.category.CategoryValue;
import org.springframework.stereotype.Service;

@Service
public class CategoryCommandService {

  private final CategoryRepository categoryRepository;

  public CategoryCommandService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Category create(CreateCategoryCommand command) {
    var category = Category.create(command.name());

    return categoryRepository.save(category);
  }

  public Category updateCategoryName(UpdateCategoryNameCommand command) {
    var category = categoryRepository.getById(command.categoryId());

    category.updateName(command.name());

    return categoryRepository.save(category);
  }

  public CategoryValue addCategoryValue(AddCategoryValueCommand command) {
    var category = categoryRepository.getById(command.categoryId());

    var categoryValue = category.addValue(command.name());

    categoryRepository.save(category);

    return categoryValue;
  }

  public CategoryValue updateCategoryValueName(UpdateCategoryValueNameCommand command) {
    var category = categoryRepository.getById(command.categoryId());

    category.updateCategoryValueName(command.categoryValueId(), command.name());

    categoryRepository.save(category);

    return category.getValue(command.categoryValueId());
  }
}
