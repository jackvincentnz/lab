package nz.geek.jack.libs.ddd.persistence;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import nz.geek.jack.libs.ddd.domain.EventSourcedAggregate;
import nz.geek.jack.libs.ddd.domain.InternalId;

public abstract class InMemoryAggregateStore<
    I extends InternalId, A extends EventSourcedAggregate<I>> {

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
