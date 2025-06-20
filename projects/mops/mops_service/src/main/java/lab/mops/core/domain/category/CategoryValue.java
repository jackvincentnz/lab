package lab.mops.core.domain.category;

import java.util.Objects;

public class CategoryValue {

  private final CategoryValueId id;

  private String name;

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

  public void updateName(String name) {
    Objects.requireNonNull(name, "name must not be null");
    this.name = name;
  }
}
