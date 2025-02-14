package nz.geek.jack.mops.core.application.category;

import java.util.Collection;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryQueryService {

  private final CategoryRepository categoryRepository;

  public CategoryQueryService(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  public Collection<Category> findAll() {
    return categoryRepository.findAll();
  }
}
