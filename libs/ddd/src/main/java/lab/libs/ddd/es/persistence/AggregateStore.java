package lab.libs.ddd.es.persistence;

import lab.libs.ddd.domain.EventSourcedAggregate;
import lab.libs.ddd.domain.InternalId;
import org.springframework.stereotype.Repository;

@Repository
public class AggregateStore {

  private final AggregateFactory aggregateFactory;

  private final EventRepository eventRepository;

  public AggregateStore(AggregateFactory aggregateFactory, EventRepository eventRepository) {
    this.aggregateFactory = aggregateFactory;
    this.eventRepository = eventRepository;
  }

  public <A extends EventSourcedAggregate<?>> A save(A aggregate) {
    var changes = aggregate.flushEvents();

    eventRepository.appendEvents(aggregate.getId().toUUID(), aggregate.getVersion(), changes);

    return aggregate;
  }

  public <A extends EventSourcedAggregate<?>> A get(
      InternalId aggregateId, Class<A> aggregateClass) {
    var events = eventRepository.readEvents(aggregateId.toUUID());

    var aggregate = aggregateFactory.forClass(aggregateClass);
    aggregate.replay(events);

    return aggregate;
  }
}
