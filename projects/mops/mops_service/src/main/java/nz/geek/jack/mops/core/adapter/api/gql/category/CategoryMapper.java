package nz.geek.jack.mops.core.adapter.api.gql.category;

import nz.geek.jack.mops.api.gql.types.Category;
import nz.geek.jack.mops.api.gql.types.CategoryValue;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

  public Category map(nz.geek.jack.mops.core.domain.category.Category category) {
    return Category.newBuilder()
        .id(category.getId().toString())
        .name(category.getName())
        .values(category.getValues().stream().map(this::map).toList())
        .build();
  }

  public CategoryValue map(nz.geek.jack.mops.core.domain.category.CategoryValue categoryValue) {
    return CategoryValue.newBuilder()
        .id(categoryValue.getId().toString())
        .name(categoryValue.getName())
        .build();
  }
}
