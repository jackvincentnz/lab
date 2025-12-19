package lab.mops.core.domain.budget;

import java.util.Objects;
import lab.mops.core.domain.category.CategoryId;
import lab.mops.core.domain.category.CategoryValueId;

public final class Categorization {

  private final CategoryId categoryId;

  private final CategoryValueId categoryValueId;

  private Categorization(CategoryId categoryId, CategoryValueId categoryValueId) {
    this.categoryId = categoryId;
    this.categoryValueId = categoryValueId;
  }

  public static Categorization of(CategoryId categoryId, CategoryValueId categoryValueId) {
    return new Categorization(categoryId, categoryValueId);
  }

  public CategoryId getCategoryId() {
    return categoryId;
  }

  public CategoryValueId getCategoryValueId() {
    return categoryValueId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    Categorization that = (Categorization) o;
    return Objects.equals(categoryId, that.categoryId)
        && Objects.equals(categoryValueId, that.categoryValueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(categoryId, categoryValueId);
  }
}
