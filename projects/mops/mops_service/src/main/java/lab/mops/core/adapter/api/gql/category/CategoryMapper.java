package lab.mops.core.adapter.api.gql.category;

import lab.mops.api.gql.types.Category;
import lab.mops.api.gql.types.CategoryValue;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

  public Category map(lab.mops.core.domain.category.Category category) {
    return Category.newBuilder()
        .id(category.getId().toString())
        .name(category.getName())
        .values(category.getValues().stream().map(this::map).toList())
        .createdAt(category.getCreatedAt().toString())
        .updatedAt(category.getUpdatedAt().toString())
        .build();
  }

  public CategoryValue map(lab.mops.core.domain.category.CategoryValue categoryValue) {
    return CategoryValue.newBuilder()
        .id(categoryValue.getId().toString())
        .name(categoryValue.getName())
        .build();
  }
}
