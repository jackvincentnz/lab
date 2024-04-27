package nz.geek.jack.libs.ddd.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nz.geek.jack.libs.ddd.domain.AbstractId;
import nz.geek.jack.libs.ddd.domain.Aggregate;

public abstract class InMemoryAggregateStore<I extends AbstractId, A extends Aggregate<I>> {

  protected final Map<I, A> aggregates = new ConcurrentHashMap<>();

  public void save(A aggregate) {
    aggregates.put(aggregate.getId(), aggregate);
  }

  public A get(I id) {
    if (aggregates.containsKey(id)) {
      return aggregates.get(id);
    }
    throw new RuntimeException(String.format("Aggregate: %s does not exist", id));
  }

  public Collection<A> getAll() {
    return aggregates.values();
  }
}
