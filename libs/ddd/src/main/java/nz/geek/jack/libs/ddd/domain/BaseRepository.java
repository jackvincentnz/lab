package nz.geek.jack.libs.ddd.domain;

import java.util.Collection;
import org.springframework.data.repository.CrudRepository;

public interface BaseRepository<T, ID extends AbstractId> extends CrudRepository<T, ID> {

  @Override
  Collection<T> findAll();

  default T getById(ID id) {
    return findById(id).orElseThrow(() -> new NotFoundException(id));
  }
}
