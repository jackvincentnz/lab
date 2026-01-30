package lab.libs.ddd.domain;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;

public interface BaseRepository<T extends Aggregate<ID>, ID extends InternalId>
    extends CrudRepository<T, ID> {

  @Override
  Collection<T> findAll();

  default Map<ID, T> mapById(Set<ID> ids) {
    var results = findAllById(ids);

    var map = new HashMap<ID, T>();
    for (var result : results) {
      map.put(result.getId(), result);
    }

    return map;
  }

  default T getById(ID id) {
    return findById(id).orElseThrow(() -> new NotFoundException(id));
  }
}
