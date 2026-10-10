package lab.mops.core.api.gql.category;

import static lab.mops.core.api.gql.category.CategoryDataLoader.NAME;

import com.netflix.graphql.dgs.DgsDataLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import lab.mops.core.application.category.CategoryQueryService;
import lab.mops.core.domain.category.Category;
import lab.mops.core.domain.category.CategoryId;
import org.dataloader.MappedBatchLoader;

@DgsDataLoader(name = NAME)
public class CategoryDataLoader implements MappedBatchLoader<CategoryId, Category> {

  public static final String NAME = "categories";

  private final CategoryQueryService categoryQueryService;

  public CategoryDataLoader(CategoryQueryService categoryQueryService) {
    this.categoryQueryService = categoryQueryService;
  }

  @Override
  public CompletionStage<Map<CategoryId, Category>> load(Set<CategoryId> ids) {
    // The batch runs on the common pool without the request's SecurityContextHolder,
    // IdentityHolder or TenantContextHolder, so nothing it calls may read them.
    return CompletableFuture.supplyAsync(() -> categoryQueryService.mapById(ids));
  }
}
