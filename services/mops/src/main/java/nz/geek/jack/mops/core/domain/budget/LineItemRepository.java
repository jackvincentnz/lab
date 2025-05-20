package nz.geek.jack.mops.core.domain.budget;

import nz.geek.jack.libs.ddd.domain.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineItemRepository extends BaseRepository<LineItem, LineItemId> {}
