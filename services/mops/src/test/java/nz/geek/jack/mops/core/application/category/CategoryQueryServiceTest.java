package nz.geek.jack.mops.core.application.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryRepository;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTest extends TestBase {

  @Mock CategoryRepository categoryRepository;

  @InjectMocks CategoryQueryService categoryQueryService;

  @Test
  void findAll_delegatesToRepository() {
    var categories = List.of(Category.create(randomString()));
    when(categoryRepository.findAll()).thenReturn(categories);

    var result = categoryQueryService.findAll();

    assertThat(result).isEqualTo(categories);
  }
}
