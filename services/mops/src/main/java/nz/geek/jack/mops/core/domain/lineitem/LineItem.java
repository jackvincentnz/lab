package nz.geek.jack.mops.core.domain.lineitem;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import nz.geek.jack.libs.ddd.domain.Aggregate;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryValue;

public class LineItem extends Aggregate<LineItemId> {

  private final String name;

  private final Set<Categorization> categorizations;

  private LineItem(LineItemId id, String name, Set<Categorization> categorizations) {
    super(id);
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(categorizations, "name must not be null");
    this.name = name;
    this.categorizations = categorizations;
  }

  public void categorize(Category category, CategoryValue categoryValue) {
    var categoryId = category.getId();
    var categoryValueId = categoryValue.getId();

    categorizations.add(Categorization.of(categoryId, categoryValueId));

    registerEvent(new LineItemCategorizedEvent(this.id, categoryId, categoryValueId));
  }

  public String getName() {
    return name;
  }

  public Set<Categorization> getCategorizations() {
    return categorizations;
  }

  public static LineItem add(String name) {
    var lineItem = new LineItem(LineItemId.create(), name, new HashSet<>());

    lineItem.registerEvent(new LineItemAddedEvent(lineItem.id, lineItem.getName()));

    return lineItem;
  }
}
