package nz.geek.jack.mops.core.application.category;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryId;
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

  public Map<CategoryId, Category> mapById(Set<CategoryId> ids) {
    return categoryRepository.mapById(ids);
  }

  public Category getById(CategoryId id) {
    return categoryRepository.getById(id);
  }
}
