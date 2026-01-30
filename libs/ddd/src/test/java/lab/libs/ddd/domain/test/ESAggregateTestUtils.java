package lab.libs.ddd.domain.test;

import lab.libs.ddd.domain.DomainEvent;
import lab.libs.ddd.domain.EventSourcedAggregate;

public class ESAggregateTestUtils {

  @SuppressWarnings("unchecked")
  public static <T extends DomainEvent> T getOnlyEventOfType(
      EventSourcedAggregate aggregate, Class<T> clazz) {
    var events = aggregate.flushEvents();

    if (events.size() > 1) {
      throw new RuntimeException("Expected only a single event");
    }

    return clazz.cast(events.get(0));
  }
}
