package nz.geek.jack.mops.core.domain.category;

import java.util.Objects;

public class CategoryValue {

  private final CategoryValueId id;

  private final String name;

  private CategoryValue(CategoryValueId id, String name) {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    this.id = id;
    this.name = name;
  }

  public CategoryValueId getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  static CategoryValue add(String name) {
    return new CategoryValue(CategoryValueId.create(), name);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    CategoryValue that = (CategoryValue) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }
}
