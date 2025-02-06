package nz.geek.jack.mops.core.domain.lineitem;

import nz.geek.jack.libs.ddd.domain.Aggregate;

public class LineItem extends Aggregate<LineItemId> {

  private final LineItemName name;

  private LineItem(LineItemId id, String name) {
    super(id);
    this.name = LineItemName.of(name);
  }

  public String getName() {
    return name.toString();
  }

  public static LineItem add(String name) {
    var lineItem = new LineItem(LineItemId.create(), name);

    lineItem.registerEvent(new LineItemAddedEvent(lineItem.id, lineItem.getName()));

    return lineItem;
  }
}
