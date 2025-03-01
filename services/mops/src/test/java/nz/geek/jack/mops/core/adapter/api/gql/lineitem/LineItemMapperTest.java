package nz.geek.jack.mops.core.adapter.api.gql.lineitem;

import static org.assertj.core.api.Assertions.assertThat;

import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.lineitem.LineItem;
import nz.geek.jack.test.TestBase;
import org.junit.jupiter.api.Test;

class LineItemMapperTest extends TestBase {

  LineItemMapper lineItemMapper = new LineItemMapper();

  @Test
  void map_mapsLineItem_id() {
    var lineItem = LineItem.add(randomString());

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getId()).isEqualTo(lineItem.getId().toString());
  }

  @Test
  void map_mapsLineItem_name() {
    var lineItem = LineItem.add(randomString());

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getName()).isEqualTo(lineItem.getName());
  }

  @Test
  void map_mapsLineItemCategorization_categoryId() {
    var lineItem = LineItem.add(randomString());
    var category = randomCategory();
    var categoryValue = category.getValues().stream().toList().get(0);
    lineItem.categorize(category, categoryValue);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getCategorizations().get(0).getCategory().getId())
        .isEqualTo(category.getId().toString());
  }

  @Test
  void map_mapsLineItemCategorization_categoryValueId() {
    var lineItem = LineItem.add(randomString());
    var category = randomCategory();
    var categoryValue = category.getValues().stream().toList().get(0);
    lineItem.categorize(category, categoryValue);

    var result = lineItemMapper.map(lineItem);

    assertThat(result.getCategorizations().get(0).getCategoryValue().getId())
        .isEqualTo(categoryValue.getId().toString());
  }

  private Category randomCategory() {
    var category = Category.create(randomString());
    category.addValue(randomString());
    return category;
  }
}
