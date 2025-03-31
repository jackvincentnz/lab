package nz.geek.jack.mops.core.domain.category;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import nz.geek.jack.libs.ddd.domain.DuplicateException;
import nz.geek.jack.libs.ddd.domain.NotFoundException;

public class Category extends Aggregate<CategoryId> {

  private String name;

  private final Set<CategoryValue> values;

  private Category(CategoryId id, String name, Set<CategoryValue> values) {
    super(id);
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(name, "Values cannot be null");
    this.name = name;
    this.values = values;
  }

  public void updateName(String name) {
    Objects.requireNonNull(name, "Name cannot be null");
    this.name = name;
    registerEvent(new CategoryNameUpdatedEvent(id, name));
  }

  public CategoryValue addValue(String name) {
    if (values.stream().map(CategoryValue::getName).toList().contains(name)) {
      throw new DuplicateException(CategoryValue.class);
    }

    var value = CategoryValue.add(name);

    values.add(value);
    registerEvent(new CategoryValueAddedEvent(value.getId(), value.getName()));

    return value;
  }

  public void updateCategoryValueName(CategoryValueId categoryValueId, String name) {
    var value = getValue(categoryValueId);
    value.updateName(name);
    registerEvent(new CategoryValueNameUpdatedEvent(id, categoryValueId, name));
  }

  public String getName() {
    return name;
  }

  public CategoryValue getValue(CategoryValueId categoryValueId) {
    var value =
        values.stream()
            .filter(categoryValue -> categoryValue.getId().equals(categoryValueId))
            .findFirst();

    return value.orElseThrow(() -> new NotFoundException(categoryValueId));
  }

  public Collection<CategoryValue> getValues() {
    return values;
  }

  public static Category create(String name) {
    var category = new Category(CategoryId.create(), name, new HashSet<>());

    category.registerEvent(new CategoryCreatedEvent(category.id, category.getName()));

    return category;
  }
}
