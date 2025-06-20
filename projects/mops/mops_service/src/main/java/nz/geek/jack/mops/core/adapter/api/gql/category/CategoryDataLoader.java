package nz.geek.jack.mops.core.adapter.api.gql.category;

import static nz.geek.jack.mops.core.adapter.api.gql.category.CategoryDataLoader.NAME;

import com.netflix.graphql.dgs.DgsDataLoader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import nz.geek.jack.mops.core.application.category.CategoryQueryService;
import nz.geek.jack.mops.core.domain.category.Category;
import nz.geek.jack.mops.core.domain.category.CategoryId;
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
    // TODO: Ensure we get a security context in the categoryQueryService by using
    //  DelegatingSecurityContextExecutor:
    //  https://www.baeldung.com/spring-security-async-principal-propagation
    //  See also:
    //  https://docs.spring.io/spring-security/reference/features/integrations/concurrency.html

    return CompletableFuture.supplyAsync(() -> categoryQueryService.mapById(ids));
  }
}
