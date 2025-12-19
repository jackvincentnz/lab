package lab.mops.core.domain.budget;

import static org.assertj.core.api.Assertions.assertThat;

import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class CategorizationTest extends TestBase {

  @Test
  void of_setsCategoryId() {
    var categoryId = CategoryId.create();

    var categorization = Categorization.of(categoryId, CategoryValueId.create());

    assertThat(categorization.getCategoryId()).isEqualTo(categoryId);
  }

  @Test
  void of_setsCategoryValueId() {
    var categoryValueId = CategoryValueId.create();

    var categorization = Categorization.of(CategoryId.create(), categoryValueId);

    assertThat(categorization.getCategoryValueId()).isEqualTo(categoryValueId);
  }

  @Test
  void equals_whenCategoryAndValueMatch() {
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();

    var a = Categorization.of(categoryId, categoryValueId);
    var b = Categorization.of(categoryId, categoryValueId);

    assertThat(a).isEqualTo(b);
  }

  @Test
  void equals_whenDifferentObject_isFalse() {
    var categorization = Categorization.of(CategoryId.create(), CategoryValueId.create());

    assertThat(categorization).isNotEqualTo(new Object());
  }

  @Test
  void hashCode_matchesWhenCategoryAndValueMatch() {
    var categoryId = CategoryId.create();
    var categoryValueId = CategoryValueId.create();

    var a = Categorization.of(categoryId, categoryValueId);
    var b = Categorization.of(categoryId, categoryValueId);

    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }
}
