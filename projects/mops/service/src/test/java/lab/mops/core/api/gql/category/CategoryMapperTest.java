package lab.mops.core.api.gql.category;

import static org.assertj.core.api.Assertions.assertThat;

import lab.mops.core.domain.category.Category;
import lab.test.TestBase;
import org.junit.jupiter.api.Test;

class CategoryMapperTest extends TestBase {
  CategoryMapper categoryMapper = new CategoryMapper();

  @Test
  void map_mapsCategory_id() {
    var category = Category.create(randomString());

    var result = categoryMapper.map(category);

    assertThat(result.getId()).isEqualTo(category.getId().toString());
  }

  @Test
  void map_mapsCategory_name() {
    var category = Category.create(randomString());

    var result = categoryMapper.map(category);

    assertThat(result.getName()).isEqualTo(category.getName());
  }

  @Test
  void map_mapsCategoryValue_id() {
    var category = Category.create(randomString());
    var value = category.addValue(randomString());

    var result = categoryMapper.map(category);

    assertThat(result.getValues().get(0).getId()).isEqualTo(value.getId().toString());
  }

  @Test
  void map_mapsCategoryValue_name() {
    var category = Category.create(randomString());
    var value = category.addValue(randomString());

    var result = categoryMapper.map(category);

    assertThat(result.getValues().get(0).getName()).isEqualTo(value.getName());
  }
}
