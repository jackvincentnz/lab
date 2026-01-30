package lab.mops.core.domain.category;

import lab.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends BaseRepository<Category, CategoryId> {}
