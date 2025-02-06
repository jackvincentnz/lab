package nz.geek.jack.mops.core.domain.lineitem;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineItemRepository extends CrudRepository<LineItem, LineItemId> {

  @Override
  Collection<LineItem> findAll();
}
