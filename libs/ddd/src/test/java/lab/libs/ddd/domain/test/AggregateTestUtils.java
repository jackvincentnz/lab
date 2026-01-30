package lab.libs.ddd.domain.test;

import lab.libs.ddd.domain.Aggregate;

public class AggregateTestUtils {

  public static <T> T getLastEvent(Aggregate<?> aggregate, Class<T> clazz) {
    var events = aggregate.domainEvents();

    var matchingEvents = events.stream().filter(clazz::isInstance).toList();

    if (matchingEvents.isEmpty()) {
      throw new RuntimeException("No events of type " + clazz.getSimpleName());
    }

    return clazz.cast(matchingEvents.get(matchingEvents.size() - 1));
  }

  public static int countEventsOfType(Aggregate<?> aggregate, Class<?> clazz) {
    return (int) aggregate.domainEvents().stream().filter(clazz::isInstance).count();
  }
}
